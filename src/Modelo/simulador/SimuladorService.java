package Modelo.simulador;

import Modelo.Patrones.SimuladorObserver;
import Modelo.grafoDirigido.GrafoSalta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject del patrón Observer.
 *
 * Centraliza:
 *  - El tick de movimiento de la flota (llamado cada 30ms desde el Timeline de VentanaControl)
 *  - El spawn de pasajeros (automático cada 8s o manual por clic en mapa)
 *  - El registro y notificación a observers
 *
 * VentanaControl se suscribe como observer y solo recibe eventos,
 * sin tener lógica de negocio mezclada.
 */
public class SimuladorService {
    
    private final ArrayList<Vehiculo>           flota;
    private final GrafoSalta                    grafo;
    private final Despachador                   despachador;
    private final List<SimuladorObserver>       observers;

    public SimuladorService(ArrayList<Vehiculo> flota,
                            GrafoSalta grafo,
                            Despachador despachador) {
        this.flota       = flota;
        this.grafo       = grafo;
        this.despachador = despachador;
        this.observers   = new CopyOnWriteArrayList<>();
    }

    // =========================================================
    // Gestión de observers
    // =========================================================

    public void addObserver(SimuladorObserver o)    { observers.add(o); }
    public void removeObserver(SimuladorObserver o) { observers.remove(o); }

    // =========================================================
    // Tick de movimiento — llamado desde KeyFrame(30ms) en VentanaControl
    // =========================================================

    /**
     * Mueve la flota un paso y notifica a todos los observers.
     * VentanaControl llama esto en su Timeline cada 30ms.
     */
    public void tick() {
        despachador.moverFlota();

        // Notificar flota actualizada
        observers.forEach(o -> o.onFlotaActualizada(flota));

        // Notificar logs acumulados y detectar viajes asignados
        for (String msj : despachador.obtenerYLimpiarLogs()) {
    if (msj.contains("[DESPACHO] Viaje asignado")) {
        try {
            int id = Integer.parseInt(msj.split("Móvil ")[1].trim().split(" ")[0]);
            observers.forEach(o -> o.onViajeAsignado(id, msj)); // tipado + texto juntos
        } catch (Exception ignored) {
            observers.forEach(o -> o.onLogRegistrado(msj)); // fallback si falla
        }
    } else {
        observers.forEach(o -> o.onLogRegistrado(msj));
    }
}
    }

    // =========================================================
    // Spawn de pasajero — llamado desde Timeline(8s) o clic manual
    // =========================================================

    /**
     * Lanza el cálculo de una solicitud de viaje en hilo secundario.
     * @param nodoManual índice del nodo elegido en el mapa, o null para aleatorio
     */
    public void spawnPasajero(Integer nodoManual) {
        new SolicitudService(flota, grafo, despachador, observers)
                .ejecutar(nodoManual);
    }

    // =========================================================
    // Getters para que VentanaControl acceda al modelo
    // =========================================================

    public ArrayList<Vehiculo> getFlota()       { return flota; }
    public GrafoSalta          getGrafo()       { return grafo; }
    public Despachador         getDespachador() { return despachador; }


}
