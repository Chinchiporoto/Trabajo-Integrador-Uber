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

/**
 * Clase principal de la aplicación JavaFX que gestiona la interfaz gráfica del sistema
 * de despacho de vehículos "ETA Salta - Centro de Operaciones".
 * 
 * <p>Esta clase extiende {@link javafx.application.Application} e inicializa la ventana
 * principal de la aplicación. Permite la inyección de datos de simulación (grafo de rutas,
 * flota de vehículos y despachador) para su visualización en la interfaz gráfica.</p>
 * 
 * @author Proyecto AYED
 * @version 1.0
 */
public class MainApp extends Application {

	/**
	 * Grafo de la ciudad de Salta inyectado desde el simulador.
	 * Contiene los nodos (intersecciones) y aristas (calles) del mapa.
	 */
    private static GrafoSalta grafoInyectado;
    
    /**
     * Flota de vehículos inyectada desde el simulador.
     * Contiene todos los vehículos de transporte a visualizar.
     */
    private static ArrayList<Vehiculo> flotaInyectada;
    
    /**
     * Despachador del sistema que gestiona el movimiento y asignación de rutas.
     */
    private static Despachador despachadorSistema;


    /**
     * Inyecta los datos de simulación en la aplicación.
     * 
     * <p>Este método estático permite pasar el grafo de la ciudad y la flota de vehículos
     * desde el simulador principal hacia la interfaz gráfica, antes de que se inicialice
     * la ventana de la aplicación.</p>
     *
     * @param g el grafo de Salta que contiene los nodos y aristas del mapa
     * @param f el ArrayList de vehículos que forman la flota a simular
     * 
     * @see GrafoSalta
     * @see Vehiculo
     */
    // Método para pasarle los datos a la interfaz gráfica
    public static void inyectarDatosSimulacion(GrafoSalta g, ArrayList<Vehiculo> f) {
        grafoInyectado = g;
        flotaInyectada = f;
    }
	
    /**
     * Inicia la aplicación JavaFX y configura la ventana principal.
     * 
     * <p>Este método es invocado por el framework JavaFX cuando la aplicación se inicia.
     * Realiza las siguientes operaciones:</p>
     * <ul>
     *   <li>Calcula el tamaño de la ventana basado en la resolución de pantalla (90% de altura con relación 16:9)</li>
     *   <li>Crea el controlador principal de la interfaz gráfica</li>
     *   <li>Renderiza el mapa base</li>
     *   <li>Si hay datos inyectados, dibuja los vehículos, rutas y configura la simulación automática</li>
     *   <li>Aplica la hoja de estilos CSS personalizada</li>
     *   <li>Muestra la ventana en pantalla</li>
     * </ul>
     *
     * @param stage la ventana principal proporcionada por JavaFX
     * 
     * @see VentanaControl
     * @see Despachador
     */
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
            controlador.dibujarVehiculosYRutas(flotaInyectada, grafoInyectado,despachadorSistema);
            controlador.actualizarConsolaFlota(flotaInyectada);
            controlador.registrarLogViaje("SISTEMA INICIADO: Flota de " + flotaInyectada.size() + " unidades en línea.");
            controlador.configurarBotonInf(flotaInyectada, grafoInyectado, despachadorSistema);
            controlador.iniciarSimulacionAutomatica(flotaInyectada, grafoInyectado, despachadorSistema);
            controlador.iniciarMotorMovimiento(flotaInyectada, grafoInyectado, despachadorSistema);
        }
        Scene scene = new Scene(controlador.getRootnodo(), ancho, alto);
        stage.setTitle("ETA Salta - Centro de Operaciones");
        stage.setScene(scene);
        stage.setResizable(true);
        
        try {
            String rutaCss = getClass().getResource("Mystyle.css").toExternalForm();
            scene.getStylesheets().add(rutaCss);
        } catch (Exception e) {}

        stage.show();
    }
    
    
}
