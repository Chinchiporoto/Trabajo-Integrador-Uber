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
    new servicio.LectorJSON().dibujarMapa("data/Mapas de Salta-20260602/CentroyMacroSALTA.geojson", graphCx, widWindow, heightWindow);
}
public StackPane getRootnodo(){
    return root;
}

}