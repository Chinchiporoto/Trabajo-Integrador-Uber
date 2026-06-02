package simulador;

import java.util.ArrayList;
import grafoDirigido.GrafoSalta;

public class Main {

    public static void main(String[] args) {

        // ── Rutas a los CSV — poné los archivos en una carpeta datos/ en la raíz del proyecto ──
        String rutaMeta   = "datos/meta_datos_nodos_2k.csv";
        String rutaMatriz = "datos/matriz_nodos_2k.csv";

        // ══════════════════════════════════════════
        // 1. CARGAR EL GRAFO
        // ══════════════════════════════════════════
        System.out.println("=== Cargando grafo de Salta... ===");
        GrafoSalta grafo = new GrafoSalta(rutaMeta, rutaMatriz);
        grafo.cargarGrafo();
        System.out.println("Grafo listo. Nodos: " + grafo.getOrden());
        System.out.println();

        // ══════════════════════════════════════════
        // 2. CREAR VEHÍCULOS en nodos al azar
        // ══════════════════════════════════════════
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

        // ══════════════════════════════════════════
        // 3. CREAR EL DESPACHADOR
        // ══════════════════════════════════════════
        Despachador despachador = new Despachador(flota, grafo);

        // ══════════════════════════════════════════
        // 4. SIMULAR SOLICITUD DE UN PASAJERO
        // ══════════════════════════════════════════
        int nodoPasajero = 75;
        System.out.println("=== Nueva solicitud ===");
        System.out.println("Pasajero en nodo " + nodoPasajero 
            + " (" + grafo.getNodo(nodoPasajero).getNombreEsquina() + ")");
        System.out.println();

        // Calcular ETAs y ordenar en la cola
        despachador.registrarDisponibles(nodoPasajero);

        // Intentar asignar
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

        // ══════════════════════════════════════════
        // 5. MOSTRAR ESTADO DE TODA LA FLOTA
        // ══════════════════════════════════════════
        System.out.println("=== Estado de la flota ===");
        despachador.muestraCovhes();

        // ══════════════════════════════════════════
        // 6. SEGUNDA SOLICITUD — para ver rechazo en acción
        // ══════════════════════════════════════════
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
        } else {
            System.out.println("✗ Sin unidades disponibles.");
        }
    }
}
