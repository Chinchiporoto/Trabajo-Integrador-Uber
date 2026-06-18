package Vista;

import Controlador.VentanaControl;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import Modelo.grafoDirigido.GrafoSalta;
import Modelo.simulador.Despachador;
import Modelo.simulador.SimuladorService;
import Modelo.simulador.Vehiculo;

import java.util.ArrayList;

/**

- Clase principal JavaFX.
- 
- Cambios respecto a la versión anterior:
- - Construye SimuladorService y lo inyecta en VentanaControl
- - VentanaControl ya no recibe flota/grafo/despachador directamente
- - Los métodos de VentanaControl que arrancaban simulación ya no
- reciben parámetros (los tienen via SimuladorService)
  */
  public class MainApp extends Application {


private static GrafoSalta        grafoInyectado;
private static ArrayList<Vehiculo> flotaInyectada;

public static void inyectarDatosSimulacion(GrafoSalta g, ArrayList<Vehiculo> f) {
    grafoInyectado = g;
    flotaInyectada = f;
}

@Override
public void start(Stage stage) {

    // =========================================================
    // 1. SPLASH SCREEN (sin cambios respecto al original)
    // =========================================================
    Stage splashStage = new Stage();
    splashStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

    javafx.scene.layout.VBox splashRoot = new javafx.scene.layout.VBox(10);
    splashRoot.getStyleClass().add("ventana-steam");
    splashRoot.setPrefWidth(420);

    javafx.scene.image.ImageView iconoSplash = new javafx.scene.image.ImageView();
    try {
        // Carga la imagen desde la carpeta img que creamos antes
        iconoSplash.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("img/pngwing.com.png")));
        iconoSplash.setFitWidth(20);  // Tamaño clásico retro
        iconoSplash.setFitHeight(20);
        iconoSplash.setSmooth(false); // Evita que se vea borroso, mantiene los píxeles duros
    } catch (Exception e) {
        System.out.println("Advertencia: No se encontró img/icono.png para el splash.");
    }

    javafx.scene.control.Label lblEstado =
        new javafx.scene.control.Label("Starting local Pick It server...");
    lblEstado.getStyleClass().add("texto-steam-estado");

    // Agrupamos el ícono y el texto en una fila horizontal
    javafx.scene.layout.HBox filaSuperior = new javafx.scene.layout.HBox(10);
    filaSuperior.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    filaSuperior.getChildren().addAll(iconoSplash, lblEstado);

    javafx.scene.layout.HBox filaAbajo = new javafx.scene.layout.HBox(10);
    filaAbajo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);


    int cantidadBloques = 24;
    javafx.scene.layout.HBox barraCarga = new javafx.scene.layout.HBox(2);
    barraCarga.getStyleClass().add("contenedor-bloques-steam");
    barraCarga.setPrefWidth(320);
    barraCarga.setPrefHeight(19);
    barraCarga.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    for (int i = 0; i < cantidadBloques; i++) {
        javafx.scene.layout.Region bloque = new javafx.scene.layout.Region();
        bloque.getStyleClass().add("bloque-steam");
        bloque.setVisible(false);
        barraCarga.getChildren().add(bloque);
    }

    javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Cancel");
    btnCancel.getStyleClass().add("button");
    btnCancel.setDisable(true);
    btnCancel.setStyle("-fx-padding: 1 10 1 10; -fx-font-size: 12px;");

    filaAbajo.getChildren().addAll(barraCarga, btnCancel);
    splashRoot.getChildren().addAll(filaSuperior, filaAbajo);

    Scene splashScene = new Scene(splashRoot);
    try {
        splashScene.getStylesheets().add(
            getClass().getResource("Mystyle.css").toExternalForm());
    } catch (Exception e) { /* CSS opcional */ }

    splashStage.setScene(splashScene);
    splashStage.show();

    // =========================================================
    // 2. TIMELINE DE PROGRESO REAL (sin cambios)
    // =========================================================
    final int totalK = (grafoInyectado != null) ? grafoInyectado.getOrden() : 1;

    javafx.animation.Timeline animacionCarga = new javafx.animation.Timeline(
        new javafx.animation.KeyFrame(javafx.util.Duration.millis(50), e -> {
            if (grafoInyectado == null) return;
            double progreso = (double) grafoInyectado.floydK / totalK;
            int bloquesActivos = (int)(progreso * cantidadBloques);
            for (int i = 0; i < cantidadBloques; i++)
                barraCarga.getChildren().get(i).setVisible(i < bloquesActivos);

            if      (progreso < 0.25) lblEstado.setText("Starting local Pick It server...");
            else if (progreso < 0.50) lblEstado.setText("Precaching resources...");
            else if (progreso < 0.75) lblEstado.setText("Parsing Garmin info...");
            else                      lblEstado.setText("Sending client info...");
        })
    );
    animacionCarga.setCycleCount(javafx.animation.Timeline.INDEFINITE);
    animacionCarga.play();

    // =========================================================
    // 3. TAREA PESADA — Floyd en hilo secundario (sin cambios)
    // =========================================================
    javafx.concurrent.Task<Void> tareaPrecarga = new javafx.concurrent.Task<>() {
        @Override
        protected Void call() {
            if (grafoInyectado != null) {
                System.out.println("=== Ejecutando Floyd-Warshall ===");
                grafoInyectado.obtenerCostoFloyd(0, 0);
                System.out.println("=== Caché completada ===");
            }
            return null;
        }

        @Override
        protected void succeeded() {
            animacionCarga.stop();
            for (int i = 0; i < cantidadBloques; i++)
                barraCarga.getChildren().get(i).setVisible(true);
            lblEstado.setText("Ready. . .");

            javafx.animation.PauseTransition pausa =
                new javafx.animation.PauseTransition(
                    javafx.util.Duration.millis(400));

            pausa.setOnFinished(ev -> {
                splashStage.close();
                abrirVentanaPrincipal(stage);
            });
            pausa.play();
        }
    };

    new Thread(tareaPrecarga).start();
}

// =========================================================
// Ensamblado de la ventana principal
// =========================================================

private void abrirVentanaPrincipal(Stage stage) {
    Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
    double alto  = pantalla.getHeight() * 0.9;
    double ancho = alto * 9.0 / 16.0;

    // 1. Construir el service (Subject del Observer)
    Despachador despachador = new Despachador(flotaInyectada, grafoInyectado);
    SimuladorService simulador = new SimuladorService(
        flotaInyectada, grafoInyectado, despachador);

    // 2. Construir el controlador (Observer)
    VentanaControl controlador = new VentanaControl(ancho, alto);

    // 3. Inyectar — VentanaControl se registra como observer
    controlador.setSimuladorService(simulador);

    // 4. Arrancar UI inicial
    controlador.renderizarMapa();
    controlador.actualizarConsolaFlota(flotaInyectada);
    controlador.registrarLogViaje(
        "SISTEMA INICIADO: Matriz calculada. " +
        flotaInyectada.size() + " unidades en línea.");

    // 5. Configurar botón (necesita simulador ya inyectado)
    controlador.configurarBotonInf();

    // 6. Arrancar timelines
    controlador.iniciarSimulacionAutomatica();
    controlador.iniciarMotorMovimiento();

    // 7. Mostrar ventana
  Scene scene = new Scene(controlador.getRootnodo(), ancho, alto);
    stage.setTitle("Pick It - Centro de Operaciones");
    stage.setScene(scene);
    stage.setResizable(true);

    // --- NUEVO: CARGAR ÍCONO DE LA APP ---
 try {
        javafx.scene.image.Image appIcon = new javafx.scene.image.Image(getClass().getResource("img/pngwing.com.png").toExternalForm());
        stage.getIcons().add(appIcon);
    } catch (Exception e) {
        System.out.println("Advertencia: No se pudo cargar el ícono de la ventana principal.");
    }

    // --- CARGAR ESTILOS STEAM (VERDE OLIVA) ---
    try {
        String rutaCss = getClass().getResource("Mystyle.css").toExternalForm();
        scene.getStylesheets().add(rutaCss);
    } catch (Exception e) { 
        System.err.println("[ERROR CRÍTICO] ¡Se perdió la ropa! No se pudo cargar Mystyle.css:");
        e.printStackTrace(); 
    }

  stage.show();
}
}
