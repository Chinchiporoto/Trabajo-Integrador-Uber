package Vista;


import Controlador.VentanaControl;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import Modelo.grafoDirigido.*;
import Modelo.simulador.*;
import java.util.ArrayList;

public class MainApp extends Application {

	//Atributos para recibir datos genéricos desde cualquier otra parte del programa
    private static GrafoSalta grafoInyectado;
    private static ArrayList<Vehiculo> flotaInyectada;

    // Método para pasarle los datos a la interfaz gráfica
    public static void inyectarDatosSimulacion(GrafoSalta g, java.util.ArrayList<Vehiculo> f) {
        grafoInyectado = g;
        flotaInyectada = f;
    }
	
    @Override
    public void start(Stage stage) {
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double alto = pantalla.getHeight() * 0.9;
        double ancho = alto * 9.0 / 16.0;

        VentanaControl controlador = new VentanaControl(ancho, alto);
        
        // 1. Dibuja el mapa base siempre
        controlador.renderizarMapa();

        // 2. Dibuja los vehículos SOLO si alguien inyectó datos antes de abrir la ventana
        if (grafoInyectado != null && flotaInyectada != null) {
            controlador.dibujarVehiculosYRutas(flotaInyectada, grafoInyectado);
        }

        Scene scene = new Scene(controlador.getRootnodo(), ancho, alto);
        stage.setTitle("ETA Salta - Centro de Operaciones");
        stage.setScene(scene);
        stage.setResizable(false);
        
        try {
            String rutaCss = getClass().getResource("Mystyle.css").toExternalForm();
            scene.getStylesheets().add(rutaCss);
        } catch (Exception e) {}

        stage.show();
    }
    
    
    
    

    
    
    
    
	
	/*
    @Override
    public void start(Stage stage) {
        
        // Redimensionamiento dinámico 9:16 adaptado al entorno físico
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double alto = pantalla.getHeight() * 0.9;
        double ancho = alto * 9.0 / 16.0;

        // Se delega el control total de la ventana a la instancia del controlador
        VentanaControl controlador = new VentanaControl(ancho, alto);
        // Se ejecuta el despliegue del trazado de calles sobre el Canvas
        controlador.renderizarMapa();

        // Montaje de la escena limpia desde el nodo del controlador
        Scene scene = new Scene(controlador.getRootnodo(), ancho, alto);
        
        stage.setTitle("ETA Salta - Centro de Operaciones");
        stage.setScene(scene);
        stage.setResizable(false);
        try {
            String rutaCss = getClass().getResource("Mystyle.css").toExternalForm();
            scene.getStylesheets().add(rutaCss);
            System.out.println("✓ CSS cargado correctamente.");
        } catch (NullPointerException e) {
            System.out.println("❌ ERROR FATAL: No se encontró Mystyle.css en la carpeta src.");
        }
        stage.show();
    }
*/
}
