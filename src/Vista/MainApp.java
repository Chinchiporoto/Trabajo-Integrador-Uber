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
    private static Despachador despachadorSistema;

    // Método para pasarle los datos a la interfaz gráfica
    public static void inyectarDatosSimulacion(GrafoSalta g, ArrayList<Vehiculo> f) {
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
        	despachadorSistema = new Despachador(flotaInyectada, grafoInyectado);
            controlador.dibujarVehiculosYRutas(flotaInyectada, grafoInyectado);
            controlador.configurarBotonInf(flotaInyectada, grafoInyectado, despachadorSistema);
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
    
    
}
