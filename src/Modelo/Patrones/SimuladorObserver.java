package Modelo.Patrones;

import Modelo.simulador.Vehiculo;
import java.util.ArrayList;

public interface SimuladorObserver {
    void onFlotaActualizada(ArrayList<Vehiculo> flota);

    void onLogRegistrado(String mensaje);

    void onViajeAsignado(int idMovil, String logMensaje);

    void onEstadoSolicitudCambiado(boolean ocupado);
}
