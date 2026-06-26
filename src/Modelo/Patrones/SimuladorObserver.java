package Modelo.Patrones;

import Modelo.simulador.Vehiculo;
import java.util.ArrayList;

/**
 * Observer del sistema de simulación.
 * VentanaControl implementa esta interfaz para recibir
 * notificaciones del SimuladorService sin acoplarse al Modelo.
 */
public interface SimuladorObserver {
    /** Se dispara cada tick (30ms) cuando la flota se movió */
    void onFlotaActualizada(ArrayList<Vehiculo> flota);

    /** Se dispara cuando el Despachador registra un log */
    void onLogRegistrado(String mensaje);

    /** Se dispara cuando se asigna un viaje exitoso */
    void onViajeAsignado(int idMovil, String logMensaje);

    /** Se dispara para habilitar/deshabilitar el botón de solicitud */
    void onEstadoSolicitudCambiado(boolean ocupado);
}
