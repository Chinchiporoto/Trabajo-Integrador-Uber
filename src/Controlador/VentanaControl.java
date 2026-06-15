package Controlador;
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
import Modelo.simulador.*; //necesitamos importar el despachador para la genereacion de nuevos nodos aleatorios
import Modelo.recursos.*;
import Modelo.grafoDirigido.*;
import java.util.ArrayList; 

public class VentanaControl{
	
	private StackPane root;
	private Canvas canvasMap;
	private GraphicsContext graphCx;
	private HBox contenedor;
	private Button bToggle;
	private Button botonSolicitud;
	private VBox pData;
	private ListView<String> listCabbie;
	private ListView <String> listTravel;

	private double dragStartX;
	private double dragStartY;

	private boolean panelVisible= false;
	private final double widePanel= 260.0;
	private final double widWindow;
	private final double heightWindow;

public VentanaControl(double wide, double  heigth){
    this.widWindow=wide;
    this.heightWindow=heigth;

    inicializaComponentes();
    bToggle.setWrapText(true);
    configurarPanelLat();
}

private void inicializaComponentes() {
    
    root = new StackPane();
    root.setStyle("-fx-border-color: #1a1c17 #7d8a6f #7d8a6f #1a1c17; -fx-border-width: 2;");

    // 1. CREAMOS EL LIENZO
    canvasMap = new Canvas(this.widWindow, this.heightWindow);
    graphCx = canvasMap.getGraphicsContext2D();
    graphCx.setFill(Color.web("#383635"));
    graphCx.fillRect(0, 0, widWindow, heightWindow);

    // 2. NUEVO: RECUADRO Y ZOOM DEL MAPA
    StackPane contenedorMapa = new StackPane();
    contenedorMapa.getChildren().add(canvasMap); 
    
    // Borde físico del mapa
    contenedorMapa.setStyle(
        "-fx-border-color: #444444; " +   
        "-fx-border-width: 3px; " +       
        "-fx-border-style: solid; " +     
        "-fx-background-color: #1E1E1E;"  
    );

    // zoom y arraste
    contenedorMapa.setOnScroll(event -> {
        event.consume(); 
        if (event.getDeltaY() == 0) return;
        double factorZoom = (event.getDeltaY() > 0) ? 1.1 : 0.9;
        canvasMap.setScaleX(canvasMap.getScaleX() * factorZoom);
        canvasMap.setScaleY(canvasMap.getScaleY() * factorZoom);
    });

    contenedorMapa.setOnMousePressed(event -> {
        dragStartX = event.getSceneX() - canvasMap.getTranslateX();
        dragStartY = event.getSceneY() - canvasMap.getTranslateY();
        contenedorMapa.setCursor(javafx.scene.Cursor.CLOSED_HAND); 
    });

    contenedorMapa.setOnMouseDragged(event -> {
        canvasMap.setTranslateX(event.getSceneX() - dragStartX);
        canvasMap.setTranslateY(event.getSceneY() - dragStartY);
    });

    contenedorMapa.setOnMouseReleased(event -> {
        contenedorMapa.setCursor(javafx.scene.Cursor.DEFAULT);
    });

    // 3. BOTÓN DE SOLICITUD EN LA PARTE INFERIOR
    this.botonSolicitud = new Button("Mandar Solicitud de Viaje");
    this.botonSolicitud.getStyleClass().add("boton-solicitud"); 

    // Contenedor "transparente" para ubicar el botón abajo en el centro
    StackPane contenedorInferior = new StackPane();
    contenedorInferior.setPadding(new javafx.geometry.Insets(0, 0, 20, 0)); // 20px de margen inferior
    contenedorInferior.setPickOnBounds(false); //Permite hacer clic a través del fondo
    // Añadimos this.botonSolicitud
    contenedorInferior.getChildren().add(this.botonSolicitud);
    StackPane.setAlignment(this.botonSolicitud, Pos.BOTTOM_CENTER); 
    
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

    pData = new VBox(15);
    pData.setPrefWidth(widePanel); 
    pData.setMinWidth(widePanel);
    pData.setMaxWidth(widePanel);
    pData.setId("panelMap");

    Label lCabbie = new Label("Taxies Status");
    lCabbie.getStyleClass().add("etiqueta-titulo");
    listCabbie = new ListView<>();
    listCabbie.getItems().addAll("Móvil 00 - DISPONIBLE", "Móvil 01 - OCUPADO", "Móvil 02 - DISPONIBLE");
    listCabbie.setPrefHeight(heightWindow * 0.35);

    Label lTrack = new Label("Track Travel");
    lTrack.getStyleClass().add("etiqueta-titulo");
    listTravel = new ListView<>();
    listTravel.getItems().addAll("Viaje #1024 asignado a M01", "Esperando nuevas solicitudes...");
    listTravel.setPrefHeight(heightWindow * 0.35);

    pData.getChildren().addAll(lCabbie, listCabbie, lTrack, listTravel);
    contenedor.getChildren().addAll(bToggle, pData);
    contenedor.setTranslateX(widePanel);

    // 5. UNIFICACION DE LOS COMPONENTES
    // Capa 1: El mapa
    // Capa 2: El botón inferior
    // Capa 3: El panel lateral TRACK
    root.getChildren().addAll(contenedorMapa, contenedorInferior, contenedor);
}

private void configurarPanelLat(){
    bToggle.setOnAction(e -> gestionarDespliegue());
}

public void configurarBotonInf(ArrayList<Vehiculo> flota, GrafoSalta grafo, Despachador despachador) {
	this.botonSolicitud.setOnAction(e -> {
        // Pasamos los parámetros al método ejecutor
        nuevaSolicitud(flota, grafo,despachador);
    });
}

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
    Transition.play();;
}

public void renderizarMapa(){
    new LectorJSON().dibujarMapa("data/Mapas de Salta-20260602/CentroyMacroSALTA.geojson", graphCx, widWindow, heightWindow);
}

public StackPane getRootnodo(){
    return root;
}

public void nuevaSolicitud(ArrayList<Vehiculo> flota, GrafoSalta grafo, Despachador despachador) {
	despachador.crearViajeAleatorio(flota, grafo);
	renderizarMapa(); 
    dibujarVehiculosYRutas(flota, grafo); 
    actualizarPanelTrack(flota);
}

private void actualizarPanelTrack(ArrayList<Vehiculo> flota) {
    // 1. Control de seguridad: Validamos que la lista visual y la flota existan
    if (this.listCabbie != null && flota != null) {
        
        // 2. Limpiar los datos anteriores del panel para no acumular texto viejo
        this.listCabbie.getItems().clear();
        
        // 3. Recorrer la flota de vehículos uno por uno
        for (Vehiculo v : flota) {
            
            // 4. Traducir el estado lógico (Enum) a un texto entendible para el usuario
            String estadoStr = (v.getState() == EstadoVehiculo.DISPONIBLE) ? "DISPONIBLE" : "OCUPADO";
            
            // 5. Formatear el ID estéticamente (ej: "Móvil 01" en vez de "Móvil 1")
            String prefijo = (v.getId() < 10) ? "0" : ""; 
            
            // 6. Insertar la nueva línea de texto en el componente ListView visual
            this.listCabbie.getItems().add("Móvil " + prefijo + v.getId() + " - " + estadoStr);
        }
    }
}


//Límites geográficos de Salta (Idénticos al LectorJSON)
	private final double LAT_MIN = -24.805;
	private final double LAT_MAX = -24.765;
	private final double LNG_MIN = -65.430;
	private final double LNG_MAX = -65.395;

private double lngAPixel(double lng, double ancho) {
 return (lng - LNG_MIN) / (LNG_MAX - LNG_MIN) * ancho;
}

private double latAPixel(double lat, double alto) {
	return (1.0 - (lat - LAT_MIN) / (LAT_MAX - LAT_MIN)) * alto;
}


//Entran como parametros la flota de vehiculos y el grafo construido
public void dibujarVehiculosYRutas(ArrayList<Vehiculo> flota, GrafoSalta grafo) {
    
    // 1. PRIMERA CAPA: Dibujar las rutas de los autos ocupados
    graphCx.setLineWidth(3.5); 
    graphCx.setStroke(Color.web("#00E5FF")); // Azul

    for (Vehiculo v : flota) {
        if (v.getState() == EstadoVehiculo.OCUPADO) {
            ArrayList<Integer> ruta = v.getRutaAsignada();
            
            if (ruta != null && ruta.size() > 1) {
                graphCx.beginPath(); 
                
                for (int i = 0; i < ruta.size(); i++) {
                    NodoMapa nodo = grafo.getNodo(ruta.get(i));
                    if (nodo != null) {
                        double x = lngAPixel(nodo.getLongitud(), widWindow);
                        double y = latAPixel(nodo.getLatitud(), heightWindow);
                        
                        if (i == 0) {
                            graphCx.moveTo(x, y); 
                        } else {
                            graphCx.lineTo(x, y); 
                        }
                    }
                }
                graphCx.stroke(); 
                
                
                //Esto necesariamente debe estar dentro del ciclo en el que realiza el calculo
                //2. SEGUNDA CAPA: Dibujar al usuario
                // Obtenemos el último nodo de la ruta (el destino)
                int idNodoDestino = ruta.get(ruta.size() - 1);
                NodoMapa nodoPasajero = grafo.getNodo(idNodoDestino);
                
                if (nodoPasajero != null) {
                    double xp = lngAPixel(nodoPasajero.getLongitud(), widWindow);
                    double yp = latAPixel(nodoPasajero.getLatitud(), heightWindow);
                    
                    // Dibujamos al pasajero de color Amarillo
                    graphCx.setFill(Color.web("#FFD700")); 
                    graphCx.fillOval(xp - 4, yp - 4, 8, 8);
                }
            }
        }
    }

    // 3. TERCERA CAPA: Dibujar todos los vehículos por encima
    for (Vehiculo v : flota) {
        NodoMapa nodo = grafo.getNodo(v.getNodoActual());
        if (nodo != null) {
            double x = lngAPixel(nodo.getLongitud(), widWindow);
            double y = latAPixel(nodo.getLatitud(), heightWindow);
            
            if (v.getState() == EstadoVehiculo.DISPONIBLE) {
                graphCx.setFill(Color.web("#39FF14")); // Verde
            } else if (v.getState() == EstadoVehiculo.OCUPADO) {
                graphCx.setFill(Color.web("#FF003C")); // Rojo
            }
            
            // El taxi es un poco más grande
            graphCx.fillOval(x - 6, y - 6, 12, 12);
        }
    }
}

}// fin clase
