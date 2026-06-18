package Modelo.simulador;

import Modelo.Patrones.DijsktraStrat;
import Modelo.Patrones.FloydStrategy;
import Modelo.Patrones.IntelligenceStrategy;
import Modelo.Patrones.SimuladorObserver;
import Modelo.grafoDirigido.GrafoSalta;
import Modelo.recursos.NodoMapa;
import Modelo.recursos.RutaAsignada;
import Modelo.servicio.LectorJSON;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que encapsula toda la lógica de cálculo de una solicitud de viaje.
 *
 * Extraído de VentanaControl.nuevaSolicitud() — el Task gigante que antes
 * vivía en la Vista ahora vive acá, en el Modelo.
 *
 * Responsabilidades:
 *  - Encontrar un nodo pasajero válido (aleatorio o manual)
 *  - Registrar el pasajero en el Despachador
 *  - Seleccionar el vehículo candidato de la cola de prioridad
 *  - Calcular ruta Fase 1 (auto → pasajero) con Dijkstra o Floyd
 *  - Calcular ruta Fase 2 (pasajero → destino final)
 *  - Aplicar el resultado al Vehiculo y notificar observers
 */
public class SolicitudService {

    private final ArrayList<Vehiculo>     flota;
    private final GrafoSalta              grafo;
    private final Despachador             despachador;
    private final List<SimuladorObserver> observers;

    public SolicitudService(ArrayList<Vehiculo> flota,
                            GrafoSalta grafo,
                            Despachador despachador,
                            List<SimuladorObserver> observers) {
        this.flota       = flota;
        this.grafo       = grafo;
        this.despachador = despachador;
        this.observers   = observers;
    }

    // =========================================================
    // API pública
    // =========================================================

    /**
     * Lanza el cálculo en un hilo secundario.
     * @param nodoManual índice del nodo elegido por el usuario, o null para aleatorio
     */
    public void ejecutar(Integer nodoManual) {
        notifyEstado(true, "Calculando Ruta...");

        javafx.concurrent.Task<Resultado> tarea = crearTarea(nodoManual);

        tarea.setOnSucceeded(e ->
            javafx.application.Platform.runLater(() -> aplicar(tarea.getValue()))
        );
        tarea.setOnFailed(e -> {
            tarea.getException().printStackTrace();
            javafx.application.Platform.runLater(() ->
                notifyEstado(false, "Mandar Solicitud de Viaje")
            );
        });

        new Thread(tarea).start();
    }

    // =========================================================
    // Task de cálculo (hilo secundario — sin tocar UI)
    // =========================================================

    private javafx.concurrent.Task<Resultado> crearTarea(Integer nodoManual) {
        return new javafx.concurrent.Task<>() {
            @Override
            protected Resultado call() throws Exception {
                return calcular(nodoManual);
            }
        };
    }

    private Resultado calcular(Integer nodoManual) throws InterruptedException {
        int      idNodoPasajero;
        NodoMapa pasajero;
        double   margen   = 0.002;
        int      maxNodos = grafo.getOrden();

        // --- 1. Elegir nodo pasajero ---
        if (nodoManual != null) {
            idNodoPasajero = nodoManual;
            pasajero       = grafo.getNodo(idNodoPasajero);
        } else {
            do {
                idNodoPasajero = (int)(Math.random() * maxNodos);
                pasajero       = grafo.getNodo(idNodoPasajero);
            } while (pasajero == null ||
                     pasajero.getLatitud()  < LectorJSON.LAT_MIN + margen ||
                     pasajero.getLatitud()  > LectorJSON.LAT_MAX - margen ||
                     pasajero.getLongitud() < LectorJSON.LNG_MIN + margen ||
                     pasajero.getLongitud() > LectorJSON.LNG_MAX - margen ||
                     grafo.esPuntoMuerto(idNodoPasajero));
        }

        // --- 2. Registrar pasajero esperando ---
        despachador.agregarPasajeroEsperando(pasajero);

        // --- 3. Delay realista en modo manual ---
        if (nodoManual != null) {
            notifyEstadoAsync("Buscando taxi...");
            Thread.sleep(2000 + (long)(Math.random() * 2000));
        }

        // --- 4. Llenar cola de prioridad con disponibles ---
      Modelo.contenedores.VehiculoPriority colaLocal = new Modelo.contenedores.VehiculoPriority();
        for (Vehiculo v : despachador.getFlotaCompleta()) {
            if (v.getState() == EstadoVehiculo.DISPONIBLE) {
                NodoMapa nAuto = grafo.getNodo(v.getNodoActual());
                v.setEta(nAuto.distanciaHaversine(pasajero));
                colaLocal.meter(v);
            }
        }

        // --- 5. Buscar candidato y calcular ruta Fase 1 ---
        Vehiculo ganador = null;
        RutaAsignada rutaFase1 = null;
        int nodoPartidaReal = -1;
        
        while (!colaLocal.estaVacia()) {
            Vehiculo candidato = (Vehiculo) colaLocal.sacar();
            if (!candidato.aceptaViaje()) continue;

            nodoPartidaReal = candidato.getNodoActual();
            if (candidato.getRutaAsignada() != null &&
                !candidato.getRutaAsignada().isEmpty())
                nodoPartidaReal = candidato.getRutaAsignada().get(0);

            NodoMapa nodoAuto = grafo.getNodo(nodoPartidaReal);
            double   distancia = candidato.getEta();

            IntelligenceStrategy strat = distancia < 1500
                    ? new DijsktraStrat()
                    : new FloydStrategy();

            RutaAsignada r = strat.calculaETA(grafo, nodoAuto, pasajero);

            if (esRutaValida(r)) {
                ganador   = candidato;
                rutaFase1 = r;
                break;
            }
        }

        // --- 6. Calcular ruta Fase 2 (pasajero → destino final) ---
        RutaAsignada rutaFase2  = null;
        int          destFinal  = -1;

        if (ganador != null) {
            int intentos = 0;
            do {
                do {
                    destFinal = (int)(Math.random() * maxNodos);
                } while (grafo.getNodo(destFinal) == null ||
                         grafo.esPuntoMuerto(destFinal)  ||
                         destFinal == idNodoPasajero);

                NodoMapa nDest  = grafo.getNodo(destFinal);
                double   dist   = pasajero.distanciaHaversine(nDest);
                IntelligenceStrategy s = dist < 1500
                        ? new DijsktraStrat()
                        : new FloydStrategy();
                rutaFase2 = s.calculaETA(grafo, pasajero, nDest);
                intentos++;
            } while (rutaFase2.getEta() >= 9999.0 && intentos < 20);
        }

        return new Resultado(ganador, rutaFase1, nodoPartidaReal,
                             pasajero, idNodoPasajero, rutaFase2, destFinal);
    }

    // =========================================================
    // Aplicar resultado (hilo UI)
    // =========================================================

    private void aplicar(Resultado r) {
        if (r.ganador != null && r.rutaFase1 != null) {
            synchronized (r.ganador) {
                despachador.setPasajeroAct(r.pasajero);
                despachador.registrarLog("[PASAJERO] Solicitud en nodo: " + r.idPasajero);

                r.ganador.getRutaAsignada().clear();
                // Si el nodo de partida difiere del nodoActual, lo agregamos primero
                if (r.nodoPartida != r.ganador.getNodoActual())
                    r.ganador.getRutaAsignada().add(r.nodoPartida);

                r.ganador.getRutaAsignada().addAll(r.rutaFase1.getCaminoNodos());
                r.ganador.setEta(r.rutaFase1.getEta());
                r.ganador.setNodoDestinoFinal(r.destFinal);
                r.ganador.setRutaFase2(r.rutaFase2.getCaminoNodos());
                r.ganador.setEtaFase2(r.rutaFase2.getEta());
                r.ganador.resetearRelojMecanico();
                r.ganador.setState(EstadoVehiculo.ENCAMINO);
                r.pasajero.setConfirmado(true);
            }
            despachador.registrarLog("[DESPACHO] Viaje asignado al Móvil " + r.ganador.getId());
            observers.forEach(o -> o.onViajeAsignado(r.ganador.getId()));
        } else {
            despachador.registrarLog("[ALERTA] No se pudo asignar ningún vehículo.");
        }

        // Notificar flota actualizada y logs acumulados
        observers.forEach(o -> o.onFlotaActualizada(flota));
        for (String msj : despachador.obtenerYLimpiarLogs())
            observers.forEach(o -> o.onLogRegistrado(msj));

        notifyEstado(false, "Mandar Solicitud de Viaje");
    }

    // =========================================================
    // Helpers
    // =========================================================

    private boolean esRutaValida(RutaAsignada r) {
        if (r.getEta() >= 9999.0) return false;
        for (int i = 0; i < r.getCaminoNodos().size() - 1; i++) {
            NodoMapa n1 = grafo.getNodo(r.getCaminoNodos().get(i));
            NodoMapa n2 = grafo.getNodo(r.getCaminoNodos().get(i + 1));
            if (n1.distanciaHaversine(n2) > 500.0) return false;
        }
        return true;
    }

    private void notifyEstado(boolean ocupado, String texto) {
        observers.forEach(o -> o.onEstadoSolicitudCambiado(ocupado, texto));
    }

    /** Versión thread-safe para llamar desde hilo secundario */
    private void notifyEstadoAsync(String texto) {
        javafx.application.Platform.runLater(() -> notifyEstado(true, texto));
    }

    // =========================================================
    // DTO interno
    // =========================================================

    static class Resultado {
        final Vehiculo     ganador;
        final RutaAsignada rutaFase1;
        final int          nodoPartida;
        final NodoMapa     pasajero;
        final int          idPasajero;
        final RutaAsignada rutaFase2;
        final int          destFinal;

        Resultado(Vehiculo g, RutaAsignada r1, int np,
                  NodoMapa p, int ip, RutaAsignada r2, int df) {
            ganador     = g;
            rutaFase1   = r1;
            nodoPartida = np;
            pasajero    = p;
            idPasajero  = ip;
            rutaFase2   = r2;
            destFinal   = df;
        }
    }
}