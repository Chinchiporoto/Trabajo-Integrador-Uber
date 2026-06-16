package Controlador;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import Modelo.servicio.*;
import Modelo.simulador.*; 
import Modelo.recursos.*;
import Modelo.grafoDirigido.*;
import java.util.ArrayList; 

/**
 * Controlador principal de la interfaz gráfica del sistema de despacho de vehículos.
 * 
 * <p>Gestiona la visualización del mapa, los vehículos, las rutas y proporciona una interfaz
 * interactiva para solicitar viajes. Incluye funcionalidades de zoom, paneo, tracking de vehículos
 * y control de la simulación automática.</p>
 * 
 * <p>Características principales:</p>
 * <ul>
 *   <li>Renderizado del mapa base y rutas de vehículos</li>
 *   <li>Sistema de zoom y paneo del mapa</li>
 *   <li>Panel lateral desplegable con información de vehículos</li>
 *   <li>Generación automática de solicitudes de viaje</li>
 *   <li>Cálculo de rutas óptimas usando estrategias (Dijkstra/Floyd)</li>
 *   <li>Seguimiento de cámara en vehículos activos</li>
 *   <li>Motor de movimiento en tiempo real</li>
 * </ul>
 * 
 * @author Proyecto AYED
 * @version 1.0
 */
public class VentanaControl{
	
	/** Contenedor principal (StackPane) que agrupa todos los componentes de la UI */
	private StackPane root;
	/** Lienzo donde se dibuja el mapa y los vehículos */
	private Canvas canvasMap;
	/** Contexto gráfico del lienzo para operaciones de dibujo */
	private GraphicsContext graphCx;
	/** Contenedor horizontal para el panel lateral desplegable */
	private HBox contenedor;
	/** Botón para desplegar/contraer el panel lateral */
	private Button bToggle;
	/** Botón para generar una nueva solicitud de viaje */
	private Button botonSolicitud;
	/** Panel vertical con información de vehículos y viajes */
	private VBox pData;
	/** Lista que muestra el estado de los taxis/vehículos */
	private ListView<String> listCabbie;
	/** Lista que muestra el registro de viajes y eventos */
	private ListView<javafx.scene.layout.HBox> listTravel;

	/** Coordenada X inicial del arrastre del mapa */
	private double dragStartX;
	/** Coordenada Y inicial del arrastre del mapa */
	private double dragStartY;

	/** Indica si el panel lateral está visible */
	private boolean panelVisible= false;
	/** Ancho del panel lateral desplegable en píxeles */
	private final double widePanel= 260.0;
	/** Ancho de la ventana en píxeles */
	private final double widWindow;
	/** Alto de la ventana en píxeles */
	private final double heightWindow;
    /** Timeline que controla la generación automática de pasajeros */
    private Timeline relojSimulador;
    /** Timeline que controla el movimiento continuo de vehículos */
    private Timeline motorMovimiento;
    /** Indica si el sistema está procesando una solicitud de viaje */
    private boolean sistemaOcupado = false;
    /** ID del vehículo actual en seguimiento de cámara */
    private int idVehiculoEnFoco = -1; 
    /** Indica si la cámara está en modo seguimiento de un vehículo */
    private boolean camaraSigueVehiculo = false;

/**
 * Constructor que inicializa el controlador de ventana.
 * 
 * <p>Configura las dimensiones de la ventana, crea e inicializa todos los componentes
 * gráficos (mapa, botones, paneles, listas) y configura los listeners de eventos.</p>
 * 
 * @param wide ancho de la ventana en píxeles
 * @param heigth alto de la ventana en píxeles
 */
public VentanaControl(double wide, double heigth){
    this.widWindow=wide;
    this.heightWindow=heigth;

    inicializaComponentes();
    bToggle.setWrapText(true);
    configurarPanelLat();
}

/**
 * Ajusta los límites de desplazamiento del mapa cuando hay zoom aplicado.
 * 
 * <p>Previene que el usuario pueda desplazar el mapa más allá de los límites
 * visibles, evitando que queden áreas en blanco alrededor del mapa.</p>
 */
private void ajustarLimitesDesplazamiento() {
    double limiteX = (widWindow * (canvasMap.getScaleX() - 1.0)) / 2.0;
    double limiteY = (heightWindow * (canvasMap.getScaleY() - 1.0)) / 2.0;

    if (canvasMap.getTranslateX() > limiteX)  canvasMap.setTranslateX(limiteX);
    if (canvasMap.getTranslateX() < -limiteX) canvasMap.setTranslateX(-limiteX);
    if (canvasMap.getTranslateY() > limiteY)  canvasMap.setTranslateY(limiteY);
    if (canvasMap.getTranslateY() < -limiteY) canvasMap.setTranslateY(-limiteY);
}

/**
 * Inicializa todos los componentes gráficos de la interfaz.
 * 
 * <p>Crea:</p>
 * <ul>
 *   <li>El lienzo del mapa con soporte para zoom y paneo</li>
 *   <li>El botón de solicitud de viaje en la parte inferior</li>
 *   <li>El panel lateral desplegable con listas de vehículos y viajes</li>
 *   <li>Configurar todos los event listeners (scroll, mouse drag, etc.)</li>
 * </ul>
 */
private void inicializaComponentes() {
    
    root = new StackPane();
    root.setId("rootContainer"); // Removido .setStyle por ID Css

    // 1. CREAMOS EL LIENZO
    canvasMap = new Canvas(this.widWindow, this.heightWindow);
    graphCx = canvasMap.getGraphicsContext2D();
    graphCx.setFill(Color.web("#383635"));
    graphCx.fillRect(0, 0, widWindow, heightWindow);

    // 2. RECUADRO Y ZOOM DEL MAPA
    StackPane contenedorMapa = new StackPane();
    contenedorMapa.setId("contenedorMapa"); // Removido .setStyle por ID Css
    contenedorMapa.getChildren().add(canvasMap); 

    // zoom y arraste
    contenedorMapa.setOnScroll(event -> {
        event.consume(); 
        if (event.getDeltaY() == 0) return; 
        
        double factorZoom = (event.getDeltaY() > 0) ? 1.1 : 0.9;
        double nuevaEscalaX = canvasMap.getScaleX() * factorZoom;
        double nuevaEscalaY = canvasMap.getScaleY() * factorZoom;
        
        if (nuevaEscalaX < 1.0 || nuevaEscalaY < 1.0) {
            canvasMap.setScaleX(1.0);
            canvasMap.setScaleY(1.0);
            canvasMap.setTranslateX(0); 
            canvasMap.setTranslateY(0);
            return;
        }
        
        if (nuevaEscalaX > 10.0) return; 

        canvasMap.setScaleX(nuevaEscalaX);
        canvasMap.setScaleY(nuevaEscalaY);
        
        ajustarLimitesDesplazamiento();
    });

    contenedorMapa.setOnMousePressed(event -> {
        dragStartX = event.getSceneX() - canvasMap.getTranslateX(); 
        dragStartY = event.getSceneY() - canvasMap.getTranslateY(); 
        contenedorMapa.setCursor(javafx.scene.Cursor.CLOSED_HAND); 
        
        if (this.camaraSigueVehiculo) {
            this.camaraSigueVehiculo = false;
            this.idVehiculoEnFoco = -1;
            System.out.println("[CÁMARA] Control manual detectado. Modo seguimiento desactivado.");
        }
    });

    contenedorMapa.setOnMouseDragged(event -> {
        if (canvasMap.getScaleX() <= 1.0) {
            canvasMap.setTranslateX(0);
            canvasMap.setTranslateY(0);
            return;
        }

        double nuevoX = event.getSceneX() - dragStartX;
        double nuevoY = event.getSceneY() - dragStartY;

        double limiteX = (widWindow * (canvasMap.getScaleX() - 1.0)) / 2.0;
        double limiteY = (heightWindow * (canvasMap.getScaleY() - 1.0)) / 2.0;

        if (nuevoX > limiteX) nuevoX = limiteX;
        if (nuevoX < -limiteX) nuevoX = -limiteX;
        if (nuevoY > limiteY) nuevoY = limiteY;
        if (nuevoY < -limiteY) nuevoY = -limiteY;

        canvasMap.setTranslateX(nuevoX);
        canvasMap.setTranslateY(nuevoY);
    });

    contenedorMapa.setOnMouseReleased(event -> {
        contenedorMapa.setCursor(javafx.scene.Cursor.DEFAULT); 
    });

    // 3. BOTÓN DE SOLICITUD EN LA PARTE INFERIOR
    this.botonSolicitud = new Button("Mandar Solicitud de Viaje");
    this.botonSolicitud.getStyleClass().add("button"); 

    StackPane contenedorInferior = new StackPane();
    contenedorInferior.setPrefHeight(100);
    contenedorInferior.setMaxHeight(100);
    contenedorInferior.setPadding(new javafx.geometry.Insets(0, 0, 20, 0)); 
    contenedorInferior.getStyleClass().add("marco-dispositivo");
    contenedorInferior.setPickOnBounds(true);
    contenedorInferior.getChildren().add(this.botonSolicitud);
    StackPane.setAlignment(this.botonSolicitud, Pos.CENTER); 
    
    // 4. PANEL LATERAL DESPLEGABLE
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

    // 5. UNIFICACION DE LOS COMPONENTES
    root.getChildren().add(contenedorMapa);
    root.getChildren().add(contenedorInferior);
    StackPane.setAlignment(contenedorInferior, Pos.BOTTOM_CENTER);
    root.getChildren().add(contenedor);
}

/**
 * Configura los eventos del panel lateral desplegable.
 * 
 * <p>Asocia el botón de toggle con la animación de apertura/cierre del panel.</p>
 */
private void configurarPanelLat(){
    bToggle.setOnAction(e -> gestionarDespliegue());
}

/**
 * Configura el evento del botón de solicitud de viaje.
 * 
 * <p>Asocia el botón inferior con el método que genera una nueva solicitud de viaje.</p>
 * 
 * @param flota ArrayList de vehículos disponibles
 * @param grafo el grafo de la ciudad (Salta)
 * @param despachador el despachador que gestiona las asignaciones
 */
public void configurarBotonInf(ArrayList<Vehiculo> flota, GrafoSalta grafo, Despachador despachador) {
	this.botonSolicitud.setOnAction(e -> {
        nuevaSolicitud(flota, grafo, despachador);
    });
}

/**
 * Gestiona la animación de apertura/cierre del panel lateral.
 * 
 * <p>Anima el panel lateral desplegable con una transición suave de 250ms.
 * Actualiza el texto del botón y el estado de visibilidad.</p>
 */
private void gestionarDespliegue(){
    TranslateTransition Transition = new TranslateTransition(Duration.millis(250), contenedor);
    if(panelVisible){
        Transition.setToX(widePanel);
        bToggle.setText("◀\nT\nR\nA\nC\nK");
        panelVisible=false;
    }
    else{
        Transition.setToX(0);
        bToggle.setText("▶\nT\nR\nA\nC\nK");
        panelVisible=true;
    }
    Transition.play();
}

/**
 * Renderiza el mapa base del sistema.
 * 
 * <p>Lee el archivo GeoJSON y dibuja las calles y zonas del mapa de Salta
 * sobre el lienzo actual.</p>
 * 
 * @see LectorJSON
 */
public void renderizarMapa(){
    new LectorJSON().dibujarMapa("data/Mapas de Salta-20260602/CentroyMacroSALTA.geojson", graphCx, widWindow, heightWindow);
}

/**
 * Retorna el nodo raíz de la escena (StackPane).
 * 
 * @return el StackPane que contiene todos los componentes gráficos
 */
public StackPane getRootnodo(){
    return root;
}

/**
 * Genera una nueva solicitud de viaje y la procesa de forma asincrónica.
 * 
 * <p>Este método:</p>
 * <ul>
 *   <li>Genera un pasajero en un nodo aleatorio válido</li>
 *   <li>Ejecuta el cálculo de rutas en un hilo secundario (Task)</li>
 *   <li>Busca el vehículo más cercano usando la estrategia apropiada (Dijkstra/Floyd)</li>
 *   <li>Asigna la ruta al vehículo seleccionado</li>
 *   <li>Actualiza la interfaz gráfica en el hilo de UI de JavaFX</li>
 * </ul>
 * 
 * <p>Previene múltiples solicitudes simultáneas usando el flag {@code sistemaOcupado}.</p>
 * 
 * @param flota ArrayList de todos los vehículos disponibles
 * @param grafo el grafo de Salta con nodos y aristas
 * @param despachador el despachador que gestiona la lógica de asignación
 * 
 * @see Vehiculo
 * @see RutaAsignada
 * @see IntelligenceStrategy
 */
public void nuevaSolicitud(ArrayList<Vehiculo> flota, GrafoSalta grafo, Despachador despachador) {
    if (this.sistemaOcupado) {
        String msg = "[SISTEMA] Saturado. Retrasando spawn...";
        System.out.println(msg);
        registrarLogViaje(msg);
        return;
    }
    this.sistemaOcupado = true;
    this.botonSolicitud.setDisable(true);
    this.botonSolicitud.setText("Calculando Ruta...");

    javafx.concurrent.Task<RutaAsignadaWrapper> tareaViaje = new javafx.concurrent.Task<>() {
        @Override
        protected RutaAsignadaWrapper call() throws Exception {
            // =================================================================
            // 1. PROCESAMIENTO PESADO EN SEGUNDO PLANO (HILO PARALELO)
            // =================================================================
            // Generamos el cliente y corremos Dijkstra/Floyd de forma aislada.
            // La CPU puede tardar lo que necesite acá sin trabar la pantalla.
            int maxNodos = grafo.getOrden();
            int nodoPasajeroAleatorio;
            Modelo.recursos.NodoMapa pasajeroCandidato;
            double margen = 0.002;

            do {
                nodoPasajeroAleatorio = (int) (Math.random() * maxNodos);
                pasajeroCandidato = grafo.getNodo(nodoPasajeroAleatorio);
            } while (pasajeroCandidato == null || 
                     pasajeroCandidato.getLatitud() < (LectorJSON.LAT_MIN + margen) || 
                     pasajeroCandidato.getLatitud() > (LectorJSON.LAT_MAX - margen) || 
                     pasajeroCandidato.getLongitud() < (LectorJSON.LNG_MIN + margen) || 
                     pasajeroCandidato.getLongitud() > (LectorJSON.LNG_MAX - margen));

            despachador.registrarDisponibles(nodoPasajeroAleatorio);
            
            // Buscamos el auto y calculamos la ruta matemática en el hilo secundario
            Vehiculo ganador = null;
            RutaAsignada rutaCalculada = null;
            int nodoPartidaReal = -1;
            
            while(!despachador.getColaDespacho().estaVacia()){
                Vehiculo candidato = (Vehiculo) despachador.getColaDespacho().sacar();
                if(candidato.aceptaViaje()){ 
                    nodoPartidaReal = candidato.getNodoActual();
                    if (candidato.getRutaAsignada() != null && !candidato.getRutaAsignada().isEmpty()) {
                        nodoPartidaReal = candidato.getRutaAsignada().get(0);
                    }
                    
                    NodoMapa nodoAuto = grafo.getNodo(nodoPartidaReal);
                    double distanciaRecta = candidato.getEta();
                    Modelo.Patrones.IntelligenceStrategy Strat = (distanciaRecta < 1500) ? 
                        new Modelo.Patrones.DijsktraStrat() : new Modelo.Patrones.FloydStrategy();
                    
                    RutaAsignada r = Strat.calculaETA(grafo, nodoAuto, pasajeroCandidato);
                    
                    boolean esValida = true;
                    if (r.getEta() >= 9999.0) {
                        esValida = false;
                    } else {
                        for (int i = 0; i < r.getCaminoNodos().size() - 1; i++) {
                            NodoMapa n1 = grafo.getNodo(r.getCaminoNodos().get(i));
                            NodoMapa n2 = grafo.getNodo(r.getCaminoNodos().get(i+1));
                            if (n1.distanciaHaversine(n2) > 500.0) {
                                esValida = false;
                                break;
                            }
                        }
                    }
                    if (esValida) {
                        ganador = candidato;
                        rutaCalculada = r;
                        break;
                    }
                }
            }
            
            // Retornamos un contenedor con los resultados del cálculo
            return new RutaAsignadaWrapper(ganador, rutaCalculada, nodoPartidaReal, pasajeroCandidato, nodoPasajeroAleatorio);
        }

        @Override
        protected void succeeded() {
            // =================================================================
            // 2. INYECCIÓN ATÓMICA EN EL HILO DE UI (CONEXIÓN SEGURA)
            // =================================================================
            // Una vez que la matemática terminó, modificamos el estado del auto
            // de golpe en un solo frame visual de JavaFX.
            RutaAsignadaWrapper resultado = getValue();
            
            javafx.application.Platform.runLater(() -> {
                if (resultado.ganador != null && resultado.ruta != null) {
                    // Seteamos el cliente activo en el despachador
                    despachador.setPasajeroAct(resultado.pasajero);
                    despachador.registrarLog("[PASAJERO] Solicitud generada en nodo: " + resultado.idNodoPasajero);
                    
                    // Re-acoplamos de forma fluida el buffer del coche ganador
                    resultado.ganador.getRutaAsignada().clear();
                    if (resultado.nodoPartidaReal != resultado.ganador.getNodoActual()) {
                        resultado.ganador.getRutaAsignada().add(resultado.nodoPartidaReal);
                    }
                    resultado.ganador.getRutaAsignada().addAll(resultado.ruta.getCaminoNodos());
                    resultado.ganador.setEta(resultado.ruta.getEta());
                    resultado.ganador.setState(EstadoVehiculo.OCUPADO);
                    
                    despachador.registrarLog("[DESPACHO] Viaje asignado al Móvil " + resultado.ganador.getId());
                } else {
                    despachador.registrarLog("[ALERTA] No se pudo asignar ningún vehículo.");
                }

                // Refrescamos la pantalla de forma limpia
                graphCx.clearRect(0, 0, widWindow, heightWindow);
                renderizarMapa(); 
                dibujarVehiculosYRutas(flota, grafo, despachador); 
                actualizarConsolaFlota(flota);
            
                for (String msj : despachador.obtenerYLimpiarLogs()) {
                    registrarLogViaje(msj);
                }
            
                botonSolicitud.setDisable(false);
                botonSolicitud.setText("Mandar Solicitud de Viaje");
                sistemaOcupado = false;
            });
        }
    };
    
    tareaViaje.setOnFailed(e -> {
        System.err.println("\n[ERROR FATAL] El hilo explotó:");
        tareaViaje.getException().printStackTrace();
        javafx.application.Platform.runLater(() -> {
            botonSolicitud.setDisable(false);
            botonSolicitud.setText("Mandar Solicitud de Viaje");
            sistemaOcupado = false;
        });
    });
    
    new Thread(tareaViaje).start();
}

// --- CLASE AUXILIAR DE CONTENEDOR (Agregala al final de tu archivo VentanaControl.java antes del último }) ---
/**
 * Clase auxiliar interna que encapsula el resultado del cálculo de ruta.
 * 
 * <p>Se utiliza como contenedor para transferir datos desde el hilo secundario
 * de cálculo al hilo de UI de JavaFX de forma segura.</p>
 */
private static class RutaAsignadaWrapper {
    /** Vehículo seleccionado para el viaje */
    Vehiculo ganador;
    /** Ruta calculada para el vehículo */
    RutaAsignada ruta;
    /** Nodo desde el cual parte el vehículo */
    int nodoPartidaReal;
    /** Nodo mapa donde inicia el pasajero */
    NodoMapa pasajero;
    /** ID del nodo donde se encuentra el pasajero */
    int idNodoPasajero;

    RutaAsignadaWrapper(Vehiculo v, RutaAsignada r, int nPartida, NodoMapa p, int idP) {
        this.ganador = v;
        this.ruta = r;
        this.nodoPartidaReal = nPartida;
        this.pasajero = p;
        this.idNodoPasajero = idP;
    }
}

/**
 * Convierte una coordenada de longitud (grados) a píxeles en el eje X.
 * 
 * @param lng longitud en grados
 * @param ancho ancho del lienzo en píxeles
 * @return posición X en píxeles relativa al lienzo
 */
private double lngAPixel(double lng, double ancho) {
    return (lng - LectorJSON.LNG_MIN) / (LectorJSON.LNG_MAX - LectorJSON.LNG_MIN) * ancho;
}

/**
 * Convierte una coordenada de latitud (grados) a píxeles en el eje Y.
 * 
 * <p>Invierte la coordenada porque el eje Y de pantalla aumenta hacia abajo,
 * mientras que la latitud aumenta hacia arriba.</p>
 * 
 * @param lat latitud en grados
 * @param alto alto del lienzo en píxeles
 * @return posición Y en píxeles relativa al lienzo
 */
private double latAPixel(double lat, double alto) {
    return (1.0 - (lat - LectorJSON.LAT_MIN) / (LectorJSON.LAT_MAX - LectorJSON.LAT_MIN)) * alto;
}

/**
 * Verifica si un nodo se encuentra dentro de los límites del mapa visible.
 * 
 * @param nodo el nodo a verificar
 * @return true si está dentro de los límites, false en caso contrario
 */
private boolean estaDentro(NodoMapa nodo) {
    return (nodo.getLatitud() >= LectorJSON.LAT_MIN && 
            nodo.getLatitud() <= LectorJSON.LAT_MAX &&
            nodo.getLongitud() >= LectorJSON.LNG_MIN && 
            nodo.getLongitud() <= LectorJSON.LNG_MAX);
}

/**
 * Dibuja todos los vehículos y sus rutas asignadas en el mapa.
 * 
 * <p>Para cada vehículo ocupado, dibuja:</p>
 * <ul>
 *   <li>Una línea cian que representa su ruta completa</li>
 *   <li>Un punto dorado que marca el destino final</li>
 *   <li>Un círculo que marca la posición actual (verde si disponible, rojo si ocupado)</li>
 * </ul>
 * 
 * @param flota ArrayList de todos los vehículos
 * @param grafo el grafo de la ciudad con información de nodos
 * @param despachador el despachador (usado para contexto, aunque no afecta el dibujo)
 */
public void dibujarVehiculosYRutas(ArrayList<Vehiculo> flota, GrafoSalta grafo, Despachador despachador) {
    graphCx.setLineWidth(3.5);
    graphCx.setStroke(Color.web("#00E5FF", 0.5)); 

    for (Vehiculo v : flota) {
        if (v.getState() == EstadoVehiculo.OCUPADO) {
            ArrayList<Integer> ruta = v.getRutaAsignada();
            
            if (ruta != null && !ruta.isEmpty()) { 
                graphCx.beginPath(); 
                
                double xAuto = lngAPixel(v.getLngDecimal(), widWindow);
                double yAuto = latAPixel(v.getLatDecimal(), heightWindow);
                graphCx.moveTo(xAuto, yAuto);
                
                int idAnterior = v.getNodoActual(); 

                for (Integer idNodo : ruta) { 
                    NodoMapa nodoActual = grafo.getNodo(idNodo); 
                
                    if (nodoActual != null && estaDentro(nodoActual)) { 
                        double x = lngAPixel(nodoActual.getLongitud(), widWindow); 
                        double y = latAPixel(nodoActual.getLatitud(), heightWindow); 
                        
                        if (grafo.getMatrizCosto().devolver(idAnterior, idNodo) != null) { 
                            graphCx.lineTo(x, y); 
                        } else {
                            graphCx.moveTo(x, y); 
                        }
                        idAnterior = idNodo; 
                    }
                }
                graphCx.stroke(); 

                NodoMapa dest = grafo.getNodo(ruta.get(ruta.size() - 1));
                if (dest != null && estaDentro(dest)) {
                    double xp = lngAPixel(dest.getLongitud(), widWindow);
                    double yp = latAPixel(dest.getLatitud(), heightWindow);
                    graphCx.setFill(Color.web("#FFD700"));
                    graphCx.fillOval(xp - 6, yp - 6, 12, 12);
                }
            }
        }
    }

    for (Vehiculo v : flota) {
        double x, y;
        if (v.getState() == EstadoVehiculo.OCUPADO) {
             x = lngAPixel(v.getLngDecimal(), widWindow);
             y = latAPixel(v.getLatDecimal(), heightWindow);
        } else {
            NodoMapa nodo = grafo.getNodo(v.getNodoActual());
            if (nodo == null) continue;
            x = lngAPixel(nodo.getLongitud(), widWindow);
            y = latAPixel(nodo.getLatitud(), heightWindow);   
        }
        graphCx.setFill(v.getState() == EstadoVehiculo.DISPONIBLE ? Color.web("#39FF14") : Color.web("#FF003C"));
        graphCx.fillOval(x - 6, y - 6, 12, 12);
    }
}

/**
 * Actualiza la lista visual de estado de todos los vehículos en el panel lateral.
 * 
 * <p>Muestra cada vehículo con su ID y estado actual (DISPONIBLE/OCUPADO).
 * Los vehículos ocupados se resaltan visualmente como "presionados".</p>
 * 
 * @param flota ArrayList de todos los vehículos a mostrar
 */
public void actualizarConsolaFlota(ArrayList<Vehiculo> flota) {
    listCabbie.getItems().clear(); 
    listCabbie.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().remove("button");
                pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("pressed"), false);
            } else {
                setText(item);
                
                if (!getStyleClass().contains("button")) {
                    getStyleClass().add("button");
                }
                
                if (item.contains("OCUPADO")) {
                    pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("pressed"), true);
                } else {
                    pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("pressed"), false);
                }
            }
            setMouseTransparent(true);
            setFocusTraversable(false);
        }
    });
    for (Vehiculo v : flota) {
        String texto = String.format("Móvil %02d - %s", v.getId(), v.getState());
        listCabbie.getItems().add(texto);
    }
}

/**
 * Registra un evento/mensaje en el panel de viajes (Track Travel).
 * 
 * <p>Cada mensaje se muestra como un botón en la lista. Si contiene "[DESPACHO] Viaje asignado",
 * permite hacer clic para activar el modo tracking de cámara en ese vehículo.</p>
 * 
 * @param mensaje el mensaje a registrar
 */
public void registrarLogViaje(String mensaje) {
    javafx.scene.layout.HBox filaContenedor = new javafx.scene.layout.HBox();
    filaContenedor.setAlignment(Pos.CENTER_LEFT);
    filaContenedor.setSpacing(5);
    
    Button botonLog = new Button(mensaje);
    botonLog.getStyleClass().add("button");
    botonLog.setMaxWidth(Double.MAX_VALUE);
    javafx.scene.layout.HBox.setHgrow(botonLog, javafx.scene.layout.Priority.ALWAYS);
    
    if (mensaje.contains("[DESPACHO] Viaje asignado")) {
        botonLog.setOnAction(e -> {
            try {
                String[] partes = mensaje.split("Móvil ");
                if (partes.length > 1) {
                    int idMovil = Integer.parseInt(partes[1].trim().substring(0, 1));
                    this.idVehiculoEnFoco = idMovil;
                    this.camaraSigueVehiculo = true;
                    System.out.println("[CÁMARA] Modo tracking interactivo activado para el Móvil " + idMovil);
                }
            } catch (Exception ex) {
                this.camaraSigueVehiculo = false;
                this.idVehiculoEnFoco = -1;
            }
        });
    } else {
        botonLog.setDisable(true); 
        botonLog.getStyleClass().add("button-directiva");
    }
    
    filaContenedor.getChildren().add(botonLog);
    listTravel.getItems().add(0, filaContenedor);
}

/**
 * Inicia la generación automática de solicitudes de viaje.
 * 
 * <p>Genera un nuevo pasajero cada 8 segundos usando una {@link Timeline} que
 * se ejecuta indefinidamente hasta que se detenga explícitamente.</p>
 * 
 * @param flota ArrayList de vehículos
 * @param grafo el grafo de la ciudad
 * @param despachador el despachador que procesa las solicitudes
 * 
 * @see #detenerSimulacionAutomatica()
 */
public void iniciarSimulacionAutomatica(ArrayList<Vehiculo> flota, GrafoSalta grafo, Despachador despachador) {
    Duration intervalo = Duration.seconds(8);
    KeyFrame evento = new KeyFrame(intervalo, e -> {
        System.out.println("\n[SIMULADOR] ---> Spawn automático de pasajero...");
        nuevaSolicitud(flota, grafo, despachador); 
    });

    relojSimulador = new Timeline(evento);
    relojSimulador.setCycleCount(Timeline.INDEFINITE);
    relojSimulador.play();
}

/**
 * Detiene la generación automática de solicitudes de viaje.
 * 
 * <p>Pausa la Timeline del simulador para que no se generen más pasajeros automáticamente.</p>
 */
public void detenerSimulacionAutomatica() {
    if (relojSimulador != null) {
        relojSimulador.stop();
    }
}

/**
 * Inicia el motor de movimiento que actualiza la posición de los vehículos.
 * 
 * <p>Ejecuta una actualización cada 30ms (aprox. 33 FPS) que:</p>
 * <ul>
 *   <li>Mueve la flota a través de sus rutas asignadas</li>
 *   <li>Implementa el tracking automático de cámara si está activado</li>
 *   <li>Redibuja el mapa y los vehículos</li>
 *   <li>Procesa los logs del despachador</li>
 * </ul>
 * 
 * <p>Ejecuta continuamente hasta que se detenga o la aplicación cierre.</p>
 * 
 * @param flota ArrayList de vehículos a mover
 * @param grafo el grafo con la información de nodos y rutas
 * @param despachador el despachador que gestiona el movimiento de la flota
 * 
 * @see Despachador#moverFlota()
 */
public void iniciarMotorMovimiento(ArrayList<Vehiculo> flota, GrafoSalta grafo, Despachador despachador) {
    Duration intervaloMovimiento = Duration.millis(30);
    
    KeyFrame tick = new KeyFrame(intervaloMovimiento, e -> {
        despachador.moverFlota();
        
        if(this.camaraSigueVehiculo && this.idVehiculoEnFoco != -1){
            Vehiculo enfocado = null;
            for(Vehiculo v : flota){
                if(v.getId() == this.idVehiculoEnFoco){
                    enfocado = v;
                    break;
                }
            }
            if(enfocado != null && enfocado.getState() == EstadoVehiculo.OCUPADO){
                double xPixelAuto = lngAPixel(enfocado.getLngDecimal(), widWindow);
                double yPixelAuto = latAPixel(enfocado.getLatDecimal(), heightWindow);
                double targetTranslateX = (widWindow / 2.0) - xPixelAuto;
                double targetTranslateY = (heightWindow / 2.0) - yPixelAuto;
                double limiteX = (widWindow * (canvasMap.getScaleX() - 1.0)) / 2.0;
                double limiteY = (heightWindow * (canvasMap.getScaleY() - 1.0)) / 2.0;
                
                if (canvasMap.getScaleX() > 1.0){
                    if (targetTranslateX > limiteX) targetTranslateX = limiteX;
                    if (targetTranslateX < -limiteX) targetTranslateX = -limiteX;
                    if (targetTranslateY > limiteY) targetTranslateY = limiteY;
                    if (targetTranslateY < -limiteY) targetTranslateY = -limiteY;
                    
                    canvasMap.setTranslateX(targetTranslateX);
                    canvasMap.setTranslateY(targetTranslateY);
                }
            } else {
                this.camaraSigueVehiculo = false;
                this.idVehiculoEnFoco = -1;
            }
        }
        
        javafx.application.Platform.runLater(() -> {
            graphCx.clearRect(0, 0, widWindow, heightWindow);
            renderizarMapa(); 
            dibujarVehiculosYRutas(flota, grafo, despachador); 
            actualizarConsolaFlota(flota);
            
            for (String msj : despachador.obtenerYLimpiarLogs()) {
                registrarLogViaje(msj);
            }
        });
    });

    motorMovimiento = new Timeline(tick);
    motorMovimiento.setCycleCount(Timeline.INDEFINITE);
    motorMovimiento.play();
}
}