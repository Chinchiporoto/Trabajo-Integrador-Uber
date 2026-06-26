package Modelo.simulador;

import java.util.ArrayList;

import Modelo.recursos.NodoMapa;
import Modelo.grafoDirigido.AbsGrafo;
import Modelo.grafoDirigido.GrafoSalta;

/**
 * Gestor central del sistema de despacho de vehículos.
 * 
 * <p>
 * Coordina la asignación inteligente de viajes a vehículos disponibles,
 * gestiona
 * el movimiento de la flota y registra todos los eventos del sistema.
 * Implementa
 * estrategias de enrutamiento adaptativas (Dijkstra para distancias cortas,
 * Floyd para largas)
 * y un sistema de patrullaje para vehículos disponibles.
 * </p>
 * 
 * <p>
 * Responsabilidades principales:
 * </p>
 * <ul>
 * <li>Registrar vehículos disponibles en una cola de prioridad (por
 * distancia)</li>
 * <li>Asignar viajes óptimos a vehículos basado en estrategia inteligente</li>
 * <li>Validar rutas antes de asignarlas (evitar puntos muertos y rutas
 * inválidas)</li>
 * <li>Mover la flota en tiempo real con interpolación suave</li>
 * <li>Patrullar vehículos disponibles por el mapa de forma aleatoria</li>
 * <li>Registrar y distribuir logs de eventos del sistema</li>
 * </ul>
 * 
 * @author Proyecto AYED
 * @version 1.0
 */
public class Despachador {
    /** Lista de todos los vehículos en el sistema. */
    protected ArrayList<Vehiculo> vehiculos;
    /** Referencia al grafo de la ciudad (Salta). */
    private AbsGrafo map;
    /** Nodo del pasajero actualmente activo. */
    private NodoMapa pasajeroAct = null;
    /** Registro temporal de eventos para la interfaz gráfica. */
    private ArrayList<String> logsTemporales = new ArrayList<>();
    /** Lista de pasajeros que esperan ser asignados. */
    private java.util.concurrent.CopyOnWriteArrayList<NodoMapa> pasajerosEsperando = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Constructor que inicializa el despachador con una flota de vehículos.
     *
     * @param a    ArrayList de vehículos que forman la flota
     * @param mapa el grafo que representa la ciudad (Salta)
     */
    public Despachador(ArrayList<Vehiculo> a, AbsGrafo mapa) {
        this.vehiculos = a;
        this.map = mapa;
    }

    /**
     * Retorna el nodo del pasajero actualmente activo.
     *
     * @return NodoMapa del pasajero actual, o null si no hay pasajero activo.
     */
    public NodoMapa getPasajeroAct() {
        return pasajeroAct;
    }

    /**
     * Retorna la lista completa de vehículos gestionados por el despachador.
     *
     * @return lista de vehículos
     */
    public ArrayList<Vehiculo> getFlotaCompleta() {
        return this.vehiculos;
    }

    /**
     * Agrega un pasajero a la lista de espera.
     *
     * @param p pasajero que espera ser asignado
     */
    public void agregarPasajeroEsperando(NodoMapa p) {
        this.pasajerosEsperando.add(p);
    }

    /**
     * Retorna la lista de pasajeros que aún esperan ser atendidos.
     *
     * @return lista de pasajeros en espera
     */
    public java.util.concurrent.CopyOnWriteArrayList<NodoMapa> getPasajerosEsperando() {
        return this.pasajerosEsperando;
    }

    /**
     * Establece el nodo del pasajero actualmente activo para despacho.
     *
     * @param pasajero NodoMapa donde se encuentra el pasajero.
     */
    public void setPasajeroAct(NodoMapa pasajero) {
        this.pasajeroAct = pasajero;
    }

    /**
     * Agrega un vehículo a la flota del despachador.
     *
     * @param a vehículo a agregar
     */
    public void cargaAutos(Vehiculo a) {
        vehiculos.add(a);
    }
    // Removemos el nodo base para destrabar el LERP

    /**
     * Muestra en consola la información de todos los vehículos.
     */
    public void muestraCoches() {
        for (int i = 0; i < this.vehiculos.size(); i++)
            System.out.println(this.vehiculos.get(i).toString());
    }

    /**
     * Registra un mensaje de evento en el sistema.
     * 
     * <p>
     * Imprime el mensaje en la consola y lo almacena temporalmente para
     * ser procesado por la interfaz gráfica.
     * </p>
     * 
     * @param msj el mensaje a registrar
     */
    public void registrarLog(String msj) {
        System.out.println(msj); // Lo seguimos imprimiendo en consola por las dudas
        this.logsTemporales.add(msj); // Lo guardamos para la interfaz
    }

    /**
     * Obtiene todos los logs temporales acumulados y limpia el buffer.
     * 
     * <p>
     * Se usa en el hilo de UI para recuperar eventos registrados desde
     * el último update y mostrarlos en la interfaz gráfica.
     * </p>
     * 
     * @return ArrayList con copias de los logs acumulados
     */
    public ArrayList<String> obtenerYLimpiarLogs() {
        ArrayList<String> copia = new ArrayList<>(this.logsTemporales);
        this.logsTemporales.clear();
        return copia;
    }

    /**
     * Actualiza el movimiento de toda la flota.
     * 
     * <p>
     * Para cada vehículo:
     * </p>
     * <ul>
     * <li><b>OCUPADO:</b> Avanza hacia el siguiente nodo de su ruta asignada.
     * Cuando llega a destino (ruta vacía), cambia a DISPONIBLE.</li>
     * <li><b>DISPONIBLE:</b> Patrulla de forma aleatoria generando una ruta de 3
     * nodos.
     * Si queda atrapado en un punto muerto, es rescatado y reubicado
     * aleatoriamente.
     * Avanza por su ruta de patrullaje.</li>
     * </ul>
     * 
     * <p>
     * Este método es invocado continuamente (cada ~30ms) para simular el movimiento
     * en tiempo real con interpolación suave (LERP) entre nodos.
     * </p>
     * 
     * @see Vehiculo#avanzarUnNodo(GrafoSalta)
     * @see GrafoSalta#obtenerVecinosValidos(int)
     * @see GrafoSalta#esPuntoMuerto(int)
     */
    public void moverFlota() {
        GrafoSalta gs = (GrafoSalta) this.map;

        for (Vehiculo v : this.vehiculos) {
            try {
                if (v.getState() == EstadoVehiculo.ENCAMINO || v.getState() == EstadoVehiculo.OCUPADO) {
                    synchronized (v) {
                        boolean llegoAlFinaldeRuta = v.avanzarUnNodo(gs);

                        if (llegoAlFinaldeRuta && v.getRutaAsignada().isEmpty()) {
                            if (v.getState() == EstadoVehiculo.ENCAMINO) {
                                int nodoRecogida = v.getNodoActual();
                                int nodoDestino = v.getNodoDestinoFinal();

                                this.registrarLog("[TRACKING] Móvil " + v.getId() + " recogió al pasajero en nodo "
                                        + nodoRecogida + ". Iniciando viaje al destino final: " + nodoDestino);

                                long idOsmRecogida = gs.getNodo(nodoRecogida).getId();
                                this.pasajerosEsperando.removeIf(p -> p.getId() == idOsmRecogida);

                                v.getRutaAsignada().clear();

                                if (v.getRutaFase2() != null) {
                                    v.getRutaAsignada().addAll(v.getRutaFase2());
                                    v.setEta(v.getEtaFase2());
                                }

                                if (!v.getRutaAsignada().isEmpty() && v.getRutaAsignada().get(0) == nodoRecogida) {
                                    v.getRutaAsignada().remove(0);
                                }

                                v.setNodoActual(nodoRecogida);
                                v.resetearRelojMecanico();
                                v.setState(EstadoVehiculo.OCUPADO);
                            }

                            else if (v.getState() == EstadoVehiculo.OCUPADO) {
                                this.registrarLog("[TRACKING] Móvil " + v.getId()
                                        + " llegó a destino final con éxito. Pasajero desembarcado.");
                                v.setState(EstadoVehiculo.DISPONIBLE);
                                v.setNodoDestinoFinal(-1);
                                v.setEta(0.0);
                            }
                        }
                    }
                }

                else if (v.getState() == EstadoVehiculo.DISPONIBLE) {
                    v.patrullar(gs);
                }

            }

            catch (Exception e) {
                System.err.println("Error detallado en Móvil " + v.getId() + ": " + e.getMessage());
                e.printStackTrace();

                v.setState(EstadoVehiculo.DISPONIBLE);
                if (v.getRutaAsignada() != null) {
                    v.getRutaAsignada().clear();
                }
                v.setNodoDestinoFinal(-1);
                v.setEta(0.0);
            }
        }
    }
}
