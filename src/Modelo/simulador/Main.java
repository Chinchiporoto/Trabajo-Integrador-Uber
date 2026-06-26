package Modelo.simulador;
import java.util.ArrayList;
import Modelo.grafoDirigido.GrafoSalta;
import Modelo.servicio.*;
import Modelo.recursos.NodoMapa;

import Vista.MainApp;
import javafx.application.Application; 

public class Main { 
    
    public static void main(String[] args) {
        String rutaMeta   = "data/meta_datos_nodos_2k.csv";
        String rutaMatriz = "data/matriz_nodos_2k.csv";
        System.out.println("=== Cargando grafo de Salta... ===");
        GrafoSalta grafo = new GrafoSalta(rutaMeta, rutaMatriz);
        grafo.cargarGrafo();
        
        // BORRÁ o COMENTÁ la línea de grafo.obtenerCostoFloyd(0,0) que pusimos acá.

        new LectorJSON().probarLecturaNodos("data/2km.json", grafo);
        ArrayList<Vehiculo> flota = new ArrayList<>();
        int cantidadTaxis = 5;
        for (int i = 0; i < cantidadTaxis; i++) { 
        int indiceAleatorio = (int) (Math.random() * grafo.getOrden());
        NodoMapa nodoInicial;
         
        double latMin = -24.795, latMax = -24.775;
        double lngMin = -65.420, lngMax = -65.400;
    do {
        indiceAleatorio = (int) (Math.random() * grafo.getOrden());
        nodoInicial = grafo.getNodo(indiceAleatorio);
        } while (nodoInicial == null || 
         nodoInicial.getLatitud() < latMin || nodoInicial.getLatitud() > latMax ||
         nodoInicial.getLongitud() < lngMin || nodoInicial.getLongitud() > lngMax);
    flota.add(new Vehiculo(i, indiceAleatorio, grafo));
}
        System.out.println("\n=== Todo listo. Encendiendo interfaz gráfica ===");
        MainApp.inyectarDatosSimulacion(grafo, flota);
        Application.launch(MainApp.class, args);
    }
}