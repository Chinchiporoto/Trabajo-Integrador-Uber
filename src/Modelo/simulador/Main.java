package simulador;
import java.util.ArrayList;
import grafoDirigido.GrafoSalta;

public class Main { 
    public static void main(String[] args) {
        String rutaMeta   = "data/meta_datos_nodos_2k.csv";
        String rutaMatriz = "data/matriz_nodos_2k.csv";
        System.out.println("=== Cargando grafo de Salta... ===");
        GrafoSalta grafo = new GrafoSalta(rutaMeta, rutaMatriz);
        grafo.cargarGrafo();
        new servicio.LectorJSON().probarLecturaNodos("data/2km.json", grafo);
        System.out.println("Grafo listo. Nodos: " + grafo.getOrden());
        System.out.println();
        System.out.println("=== Registrando vehículos ===");
        ArrayList<Vehiculo> flota = new ArrayList<>();
        int[] nodosIniciales = {10, 50, 100, 200, 350};
        for (int i = 0; i < nodosIniciales.length; i++) {
            Vehiculo v = new Vehiculo(i, nodosIniciales[i]);
            flota.add(v);
            System.out.println("Registrado: Movil-" + i 
                + " en nodo " + nodosIniciales[i] 
                + " (" + grafo.getNodo(nodosIniciales[i]).getNombreEsquina() + ")");
        }
        System.out.println();
        Despachador despachador = new Despachador(flota, grafo);
        int nodoPasajero = 75;
        System.out.println("=== Nueva solicitud ===");
        System.out.println("Pasajero en nodo " + nodoPasajero 
            + " (" + grafo.getNodo(nodoPasajero).getNombreEsquina() + ")");
        System.out.println();
        despachador.registrarDisponibles(nodoPasajero);
        System.out.println("=== Intentando asignar vehículo ===");
        Vehiculo asignado = despachador.asignaViaje(nodoPasajero);
        System.out.println();
        if (asignado != null) {
            System.out.println("✓ Vehículo asignado: " + asignado);
            System.out.println("  Desde: " + grafo.getNodo(asignado.getNodoActual()).getNombreEsquina());
            System.out.println("  ETA:   " + String.format("%.1f", asignado.getEta()) + " segundos"
                + " (" + String.format("%.1f", asignado.getEta() / 60.0) + " minutos)");
        } else {
            System.out.println("✗ Sin unidades disponibles.");
        }
        System.out.println();
        System.out.println("=== Estado de la flota ===");
        despachador.muestraCovhes();
        System.out.println();
        int nodoPasajero2 = 300;
        System.out.println("=== Segunda solicitud ===");
        System.out.println("Pasajero en nodo " + nodoPasajero2
            + " (" + grafo.getNodo(nodoPasajero2).getNombreEsquina() + ")");
        despachador.registrarDisponibles(nodoPasajero2);
        Vehiculo asignado2 = despachador.asignaViaje(nodoPasajero2);
        System.out.println();
        if (asignado2 != null) {
           System.out.println("✓ Vehículo asignado: " + asignado2);
            System.out.println("  Desde: " + grafo.getNodo(asignado2.getNodoActual()).getNombreEsquina());
            System.out.println("  ETA:   " + String.format("%.1f", asignado2.getEta()) + " segundos"
                + " (" + String.format("%.1f", asignado2.getEta() / 60.0) + " minutos)");
        } else {
            System.out.println("✗ Sin unidades disponibles.");
        }
    }
}
