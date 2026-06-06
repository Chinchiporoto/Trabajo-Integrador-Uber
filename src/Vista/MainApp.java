package Vista;


import Controlador.VentanaControl;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

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

    public static void main(String[] args) {
        launch(args);
    }
}
