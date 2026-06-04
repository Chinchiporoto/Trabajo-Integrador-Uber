package Vista;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
public class MainApp extends Application {
@Override
public void start(Stage stage) {
   // 1. Calculamos el tamaño 9:16 dinámico
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        
        // Multiplicamos por 0.9 para que la ventana no tape la barra de tareas de tu PC
        double alto = pantalla.getHeight() * 0.9; 
        double ancho = alto * 9.0 / 16.0;
        
        // 2. Preparamos el lienzo
        Canvas canvas = new Canvas(ancho, alto);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 3. Fondo con estética retro VGUI (Gris oscuro en vez de azul)
        gc.setFill(Color.web("#383635"));
        gc.fillRect(0, 0, ancho, alto);
        
        // 4. Dibujamos el mapa leyendo tu archivo GeoJSON
        new servicio.LectorJSON().dibujarMapa(
            "data/Mapas de Salta-20260602/CentroyMacroSALTA.geojson",
            gc, ancho, alto
        );

        // 5. Armamos la ventana
        StackPane root = new StackPane(canvas);
        
        // Le agregamos el borde 3D hundido por código directo para este "Hola Mundo"
        root.setStyle("-fx-border-color: #1a1c17 #7d8a6f #7d8a6f #1a1c17; -fx-border-width: 2;");

        Scene scene = new Scene(root, ancho, alto);
        
        stage.setTitle("ETA Salta - Hola Mundo Mapa");
        stage.setScene(scene);
        stage.setResizable(false); // Bloqueamos el tamaño para no romper la proporción 9:16
        stage.show();
    }

    // El main es necesario para correrlo suelto
    public static void main(String[] args) {
        launch(args);
    }
}