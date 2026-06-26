package Controlador;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.layout.Priority;
import Modelo.Patrones.SimuladorObserver;
import Modelo.simulador.EstadoVehiculo;
import Modelo.simulador.SimuladorService;
import Modelo.simulador.Vehiculo;

import Vista.MapaView;

import java.util.ArrayList;

/**
 * Controlador principal de la interfaz gráfica.
 *
 * Post-refactor: implementa SimuladorObserver y solo contiene:
 * - Construcción y ensamblado del layout
 * - Panel lateral desplegable
 * - Timelines (motor 30ms y simulación 8s) — solo llaman al SimuladorService
 * - Implementación de SimuladorObserver: reacciona a eventos del Modelo
 * - Actualización de listas y log (solo UI)
 * - Modo selección manual de pasajero en el mapa
 *
 * Todo lo que antes era lógica de negocio (Task, cálculo de rutas,
 * asignación de vehículos) fue extraído a SimuladorService y SolicitudService.
 *
 * @see SimuladorService
 * @see Modelo.simulador.SolicitudService
 * @see MapaView
 */
public class VentanaControl implements SimuladorObserver {

    // --- Vista del mapa ---
    private MapaView mapaView;

    // --- Layout ---
    private StackPane root;
    private HBox contenedor;
    private Button bToggle;
    private VBox pData;

    // --- Listas del panel lateral ---
    private ListView<String> listCabbie;
    private ListView<HBox> listTravel;

    // --- Botón inferior ---
    private Button botonSolicitud;

    // --- Estado UI ---
    private boolean panelVisible = false;
    private boolean modoSeleccionManual = false;
    private boolean camaraSigueVehiculo = false;
    private int idVehiculoEnFoco = -1;

    // --- Dimensiones ---
    private final double widWindow;
    private final double heightWindow;

    // --- Service inyectado ---
    private SimuladorService simulador;
    private javafx.animation.Timeline motorMovimiento;
    private javafx.animation.Timeline relojSimulador;
    private final double widePanel = 260.0;

    // =========================================================
    // Constructor
    // =========================================================

    public VentanaControl(double wide, double height) {
        this.widWindow = wide;
        this.heightWindow = height - 100;

        inicializarComponentes();
        configurarPanelLat();
    }

    // =========================================================
    // Inyección del service (conecta Observer)
    // =========================================================

    /**
     * Recibe el SimuladorService ya construido y se registra como observer.
     * Debe llamarse antes de iniciarMotorMovimiento() y
     * iniciarSimulacionAutomatica().
     */
    public void setSimuladorService(SimuladorService s) {
        this.simulador = s;
        s.addObserver(this);
    }

    // =========================================================
    // Implementación SimuladorObserver
    // =========================================================

    /**
     * El Modelo avisó que la flota se movió → redibujar canvas y actualizar lista.
     * Siempre se recibe en el hilo de JavaFX (notificado desde Platform.runLater en
     * tick).
     */
    @Override
    public void onFlotaActualizada(ArrayList<Vehiculo> flota) {
        mapaView.limpiar();
        mapaView.dibujarVehiculosYRutas(flota,
                simulador.getGrafo(),
                simulador.getDespachador());
        actualizarConsolaFlota(flota);

        // Tracking de cámara: si hay vehículo en foco y sigue activo, centrar
        if (camaraSigueVehiculo && idVehiculoEnFoco != -1) {
            for (Vehiculo v : flota) {
                if (v.getId() == idVehiculoEnFoco) {
                    if (v.getState() == EstadoVehiculo.OCUPADO ||
                            v.getState() == EstadoVehiculo.ENCAMINO) {
                        mapaView.centrarCamara(v.getLatDecimal(), v.getLngDecimal());
                    } else {
                        camaraSigueVehiculo = false;
                        idVehiculoEnFoco = -1;
                    }
                    break;
                }
            }
        }
    }

    /** El Modelo registró un log → mostrarlo en el panel lateral */
    @Override
    public void onLogRegistrado(String mensaje) {
        registrarLogViaje(mensaje);
    }

    /**
     * Se asignó un viaje → activar tracking de cámara sobre ese móvil.
     * El botón del log de "[DESPACHO]" también activa el tracking al clickearlo,
     * pero este callback lo activa automáticamente sin necesitar clic del usuario.
     */
    private void registrarLogDespacho(String mensaje, int idMovil) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setSpacing(5);

        Button btn = new Button(mensaje);
        btn.getStyleClass().add("button");
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);

        btn.setOnAction(e -> {
            this.idVehiculoEnFoco = idMovil; // ← int directo, sin parsing
            this.camaraSigueVehiculo = true;
            System.out.println("[CÁMARA] Tracking activado — Móvil " + idMovil);
        });

        fila.getChildren().add(btn);
        listTravel.getItems().add(0, fila);
    }

    @Override
    public void onViajeAsignado(int idMovil, String logMensaje) {
        registrarLogDespacho(logMensaje, idMovil);
    }

    /** El SolicitudService cambió de estado → actualizar botón */
    @Override
    public void onEstadoSolicitudCambiado(boolean ocupado) {
        botonSolicitud.setDisable(ocupado);

        if (ocupado) {
            botonSolicitud.setText("Calculando...");
        } else {
            // Cuando se libera, respetamos en qué modo estaba el usuario
            if (modoSeleccionManual) {
                botonSolicitud.setText("Hacé clic en el mapa para ubicar al usuario...");
            } else {
                botonSolicitud.setText("Mandar Solicitud de Viaje");
            }
        }
    }

    // =========================================================
    // Timelines — solo coordinan, no calculan nada
    // =========================================================

    /**
     * Inicia el motor de movimiento: llama simulador.tick() cada 30ms.
     * El tick mueve la flota y notifica a este Observer vía onFlotaActualizada.
     */

    // =========================================================
    // Modo selección manual de pasajero en el mapa
    // =========================================================

    /**
     * Configura el botón inferior para alternar entre modo automático y modo
     * selección manual de pasajero en el mapa.
     */
    public void configurarBotonInf() {
        botonSolicitud.setOnAction(e -> {
            if (!modoSeleccionManual) {
                // Activar modo manual
                modoSeleccionManual = true;
                detenerSimulacionAutomatica();
                botonSolicitud.setText("Hacé clic en el mapa para ubicar al usuario...");
                botonSolicitud.setStyle("-fx-background-color: #c4b550; -fx-text-fill: #1a1c17;");
            } else {
                // Desactivar modo manual
                modoSeleccionManual = false;
                iniciarSimulacionAutomatica();
                botonSolicitud.setText("Mandar Solicitud de Viaje");
                botonSolicitud.setStyle("");
            }
        });

        // Click en el canvas: solo actúa si estamos en modo selección manual
        mapaView.setOnMapaClicked(event -> {
            System.out.println("=== [DEBUG] CLIC EN EL MAPA DETECTADO ===");

            if (!modoSeleccionManual) {
                System.out.println("[DEBUG] Ignorado: No está en modo manual.");
                return;
            }

            try {
                double lat = mapaView.pixelALat(event.getY());
                double lng = mapaView.pixelALng(event.getX());
                System.out.println("[DEBUG] Coordenadas: Lat " + lat + " | Lng " + lng);

                // ¡Magia! Le preguntamos al Modelo directamente:
                int nodoCercano = simulador.getGrafo().buscarNodoMasCercano(lat, lng);
                System.out.println("[DEBUG] Esquina más cercana encontrada: " + nodoCercano);

                if (nodoCercano != -1) {
                    modoSeleccionManual = false;
                    botonSolicitud.setStyle("");
                    botonSolicitud.setText("Mandar Solicitud de Viaje"); // <-- ¡Me había faltado esta línea!

                    System.out.println("[DEBUG] ¡Lanzando viaje al SolicitudService!");
                    simulador.spawnPasajero(nodoCercano);
                    iniciarSimulacionAutomatica();
                } else {
                    System.out.println("[DEBUG] Error: No se encontró ningún nodo válido cerca.");
                }
            } catch (Exception ex) {
                System.err.println("[ERROR CRÍTICO] Falló el evento de clic en el mapa:");
                ex.printStackTrace(); // Esto nos dirá la línea exacta si algo explota
            }
        });

        // Si el usuario arrastra el mapa, desactivar tracking de cámara
        mapaView.setOnDragIniciado(() -> {
            if (this.camaraSigueVehiculo) {
                this.camaraSigueVehiculo = false;
                this.idVehiculoEnFoco = -1;
                System.out.println("[CÁMARA] Control manual. Tracking desactivado.");
            }
        });
    }

    // =========================================================
    // Actualización de listas (pura UI, sin lógica de negocio)
    // =========================================================

    public void actualizarConsolaFlota(ArrayList<Vehiculo> flota) {
        listCabbie.getItems().clear();

        // Instanciamos nuestra nueva clase limpia desde la Vista
        listCabbie.setCellFactory(lv -> new Vista.CeldaTaxiFactory());

        for (Vehiculo v : flota) {
            listCabbie.getItems().add(
                    String.format("Móvil %02d - %s", v.getId(), v.getState()));
        }
    }

    public void registrarLogViaje(String mensaje) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setSpacing(5);

        Button btn = new Button(mensaje);
        btn.getStyleClass().add("button");
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);

        btn.setDisable(true);
        btn.getStyleClass().add("button-directiva");

        fila.getChildren().add(btn);
        listTravel.getItems().add(0, fila);
    }

    // =========================================================
    // API pública para MainApp
    // =========================================================

    public StackPane getRootnodo() {
        return root;
    }

    public void renderizarMapa() {
        mapaView.renderizarMapa();
    }

    // =========================================================
    // Construcción del layout (igual que antes, sin cambios)
    // =========================================================

    private void inicializarComponentes() {
        root = new StackPane();
        root.setId("rootContainer");
        // 1. MAPA
        mapaView = new MapaView(widWindow, heightWindow);

        // 2. BARRA INFERIOR
        this.botonSolicitud = new Button("Mandar Solicitud de Viaje");
        this.botonSolicitud.getStyleClass().add("button");

        StackPane contenedorInferior = new StackPane();
        contenedorInferior.setPrefHeight(100);
        contenedorInferior.setMaxHeight(100);
        contenedorInferior.setMinHeight(100);
        contenedorInferior.setPadding(new javafx.geometry.Insets(0));
        contenedorInferior.getStyleClass().add("marco-dispositivo");
        contenedorInferior.setPickOnBounds(true);
        contenedorInferior.getChildren().add(this.botonSolicitud);
        StackPane.setAlignment(this.botonSolicitud, Pos.CENTER);

        // 3. PANEL LATERAL
        contenedor = new HBox();
        contenedor.setAlignment(Pos.TOP_RIGHT);
        contenedor.setPickOnBounds(false);
        contenedor.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
        StackPane.setAlignment(contenedor, Pos.TOP_RIGHT);

        bToggle = new Button("◀\nT\nR\nA\nC\nK");
        bToggle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        bToggle.setWrapText(true);
        bToggle.setPrefHeight(150);
        bToggle.setPrefWidth(30);
        bToggle.getStyleClass().add("button");

        pData = new VBox(15);
        pData.setPrefWidth(widePanel);
        pData.setMinWidth(widePanel);
        pData.setMaxWidth(widePanel);
        pData.setId("panelMap");

        Label lCabbie = new Label("Taxies Status");
        lCabbie.getStyleClass().add("etiqueta-titulo");
        listCabbie = new ListView<>();
        listCabbie.setPrefHeight(heightWindow * 0.35);

        Label lTrack = new Label("Track Travel");
        lTrack.getStyleClass().add("etiqueta-titulo");
        listTravel = new ListView<>();
        listTravel.setPrefHeight(heightWindow * 0.35);

        pData.getChildren().addAll(lCabbie, listCabbie, lTrack, listTravel);
        contenedor.getChildren().addAll(bToggle, pData);
        contenedor.setTranslateX(widePanel);

        // 4. ENSAMBLADO FINAL
        VBox distribucionVertical = new VBox();
        distribucionVertical.getChildren().addAll(
                mapaView.getContenedor(), contenedorInferior);

        root.getChildren().add(distribucionVertical);
        root.getChildren().add(contenedor);
        StackPane.setAlignment(contenedor, Pos.TOP_RIGHT);
    }

    private void configurarPanelLat() {
        bToggle.setOnAction(e -> gestionarDespliegue());
    }

    private void gestionarDespliegue() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), contenedor);
        if (panelVisible) {
            tt.setToX(widePanel);
            bToggle.setText("◀\nT\nR\nA\nC\nK");
            panelVisible = false;
        } else {
            tt.setToX(0);
            bToggle.setText("▶\nT\nR\nA\nC\nK");
            panelVisible = true;
        }
        tt.play();
    }

    public void iniciarMotorMovimiento() {
        motorMovimiento = new Timeline(
                new KeyFrame(Duration.millis(16), e -> simulador.tick()));
        motorMovimiento.setCycleCount(Timeline.INDEFINITE);
        motorMovimiento.play();
    }

    public void iniciarSimulacionAutomatica() {
        relojSimulador = new Timeline(
                new KeyFrame(Duration.seconds(8), e -> {
                    System.out.println("\n[SIMULADOR] ---> Spawn automático de pasajero...");
                    simulador.spawnPasajero(null);
                }));
        relojSimulador.setCycleCount(Timeline.INDEFINITE);
        relojSimulador.play();
    }

    public void detenerSimulacionAutomatica() {
        if (relojSimulador != null)
            relojSimulador.stop();
    }
}
