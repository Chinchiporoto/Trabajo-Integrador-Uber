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
import Modelo.simulador.*;
import Modelo.recursos.*;
import Modelo.grafoDirigido.*;
import java.util.ArrayList;

public class VentanaControl{
private StackPane root;
private Canvas canvasMap;
private GraphicsContext graphCx;
private HBox contenedor;
private Button bToggle;
private VBox pData;
private ListView<String> listCabbie;
private ListView <String> listTravel;

private boolean panelVisible= false;
private final double widePanel= 260.0;
private final double widWindow;
private final double heightWindow;

public VentanaControl(double wide, double  heigth){
    this.widWindow=wide;
    this.heightWindow=heigth;

    inicializaComponentes();
    bToggle.setWrapText(true);
    configurarEventos();
}
private void inicializaComponentes(){
    root = new StackPane();
    root.setStyle("-fx-border-color: #1a1c17 #7d8a6f #7d8a6f #1a1c17; -fx-border-width: 2;");

    canvasMap= new Canvas(this.widWindow,this.heightWindow);
    graphCx= canvasMap.getGraphicsContext2D();

    graphCx.setFill(Color.web("#383635"));
    graphCx.fillRect(0,0, widWindow, heightWindow);

    contenedor= new HBox();
    contenedor.setAlignment(Pos.TOP_RIGHT);
    contenedor.setPickOnBounds(false);
    contenedor.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
    StackPane.setAlignment(contenedor, Pos.TOP_RIGHT);

    bToggle=new Button("◀\nT\nR\nA\nC\nK");
    bToggle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
    bToggle.setWrapText(true);
    bToggle.setPrefHeight(150);
    bToggle.setPrefWidth(30);

    pData= new VBox(15);
    pData.setPrefWidth(widePanel); 
    pData.setMinWidth(widePanel);
    pData.setMaxWidth(widePanel);
    pData.setId("panelMap");

    Label lCabbie= new Label("Taxies Status");
    lCabbie.getStyleClass().add("etiqueta-titulo");
    listCabbie= new ListView<>();
    listCabbie.getItems().addAll("Móvil 00 - DISPONIBLE", "Móvil 01 - OCUPADO", "Móvil 02 - DISPONIBLE");
    listCabbie.setPrefHeight(heightWindow*0.35);

    Label lTrack= new Label("Track Travel");
    lTrack.getStyleClass().add("etiqueta-titulo");
    listTravel= new ListView<>();
    
    listTravel.getItems().addAll("Viaje #1024 asignado a M01", "Esperando nuevas solicitudes...");
    listTravel.setPrefHeight(heightWindow * 0.35);

    pData.getChildren().addAll(lCabbie, listCabbie, lTrack, listTravel);
    contenedor.getChildren().addAll(bToggle, pData);
    contenedor.setTranslateX(widePanel);
    root.getChildren().addAll(canvasMap,contenedor);;
}
private void configurarEventos(){
    bToggle.setOnAction(e -> gestionarDespliegue());
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
                graphCx.setFill(Color.web("#39FF14")); // Verde chillón
            } else if (v.getState() == EstadoVehiculo.OCUPADO) {
                graphCx.setFill(Color.web("#FF003C")); // Rojo intenso
            }
            
            // El taxi es un poco más grande (12x12 píxeles)
            graphCx.fillOval(x - 6, y - 6, 12, 12);
        }
    }
}

}












