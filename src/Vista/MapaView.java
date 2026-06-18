package Vista;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

import Modelo.grafoDirigido.GrafoSalta;
import Modelo.recursos.NodoMapa;
import Modelo.servicio.LectorJSON;
import Modelo.simulador.Despachador;
import Modelo.simulador.EstadoVehiculo;
import Modelo.simulador.Vehiculo;

/**
 * Vista del mapa de la ciudad.
 *
 * <p>Responsabilidades exclusivas de esta clase:</p>
 * <ul>
 *   <li>Crear y mantener el Canvas y su GraphicsContext</li>
 *   <li>Zoom y paneo del mapa con límites correctos</li>
 *   <li>Renderizar el mapa base (GeoJSON)</li>
 *   <li>Dibujar vehículos, rutas y pasajeros</li>
 *   <li>Conversión coordenadas geográficas ↔ píxeles</li>
 *   <li>Centrado de cámara sobre una posición</li>
 * </ul>
 *
 * <p>No conoce lógica de negocio — solo sabe dibujar lo que le pasan.</p>
 *
 * @author Proyecto AYED
 * @version 1.0
 */
public class MapaView {

    private final Canvas       canvasMap;
    private final GraphicsContext graphCx;
    private final StackPane    contenedorMapa;

    private final double widWindow;
    private final double heightWindow;
    private javafx.scene.image.Image imgPasajero;
    /** Puntos de inicio del arrastre para el paneo */
    private double dragStartX;
    private double dragStartY;

    /** Callback que se dispara cuando el usuario presiona y arrastra (para desactivar cámara) */
    private Runnable onDragIniciado;

    // ------------------------------------------------------------------

    public MapaView(double width, double height) {
        this.widWindow    = width;
        this.heightWindow = height;

        // Canvas
        canvasMap = new Canvas(widWindow, heightWindow);
        graphCx   = canvasMap.getGraphicsContext2D();
        graphCx.setFill(Color.web("#383635"));
        graphCx.fillRect(0, 0, widWindow, heightWindow);

        // Contenedor con ID CSS
        contenedorMapa = new StackPane();
        contenedorMapa.setId("contenedorMapa");
        contenedorMapa.getChildren().add(canvasMap);

        configurarZoom();
        configurarPaneo();
        try {
            imgPasajero = new javafx.scene.image.Image(getClass().getResourceAsStream("img/Code_Generated_Image.png"));
        } catch (Exception e) {
            System.err.println("Advertencia: No se encontró ");
        }
    }

    // ------------------------------------------------------------------
    // Configuración interna
    // ------------------------------------------------------------------

    private void configurarZoom() {
        contenedorMapa.setOnScroll(event -> {
            event.consume();
            if (event.getDeltaY() == 0) return;

            double factor     = (event.getDeltaY() > 0) ? 1.1 : 0.9;
            double nuevaEscX  = canvasMap.getScaleX() * factor;
            double nuevaEscY  = canvasMap.getScaleY() * factor;

            if (nuevaEscX < 1.0 || nuevaEscY < 1.0) {
                canvasMap.setScaleX(1.0);
                canvasMap.setScaleY(1.0);
                canvasMap.setTranslateX(0);
                canvasMap.setTranslateY(0);
                return;
            }
            if (nuevaEscX > 10.0) return;

            canvasMap.setScaleX(nuevaEscX);
            canvasMap.setScaleY(nuevaEscY);
            ajustarLimites();
        });
    }

    private void configurarPaneo() {
        contenedorMapa.setOnMousePressed(event -> {
            dragStartX = event.getSceneX() - canvasMap.getTranslateX();
            dragStartY = event.getSceneY() - canvasMap.getTranslateY();
            contenedorMapa.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            if (onDragIniciado != null) onDragIniciado.run();
        });

        contenedorMapa.setOnMouseDragged(event -> {
            if (canvasMap.getScaleX() <= 1.0) {
                canvasMap.setTranslateX(0);
                canvasMap.setTranslateY(0);
                return;
            }
            double nuevoX = event.getSceneX() - dragStartX;
            double nuevoY = event.getSceneY() - dragStartY;

            double limX = (widWindow    * (canvasMap.getScaleX() - 1.0)) / 2.0;
            double limY = (heightWindow * (canvasMap.getScaleY() - 1.0)) / 2.0;

            canvasMap.setTranslateX(Math.max(-limX, Math.min(limX, nuevoX)));
            canvasMap.setTranslateY(Math.max(-limY, Math.min(limY, nuevoY)));
        });

        contenedorMapa.setOnMouseReleased(event ->
            contenedorMapa.setCursor(javafx.scene.Cursor.DEFAULT)
        );
    }

    private void ajustarLimites() {
        double limX = (widWindow    * (canvasMap.getScaleX() - 1.0)) / 2.0;
        double limY = (heightWindow * (canvasMap.getScaleY() - 1.0)) / 2.0;

        if (canvasMap.getTranslateX() >  limX) canvasMap.setTranslateX( limX);
        if (canvasMap.getTranslateX() < -limX) canvasMap.setTranslateX(-limX);
        if (canvasMap.getTranslateY() >  limY) canvasMap.setTranslateY( limY);
        if (canvasMap.getTranslateY() < -limY) canvasMap.setTranslateY(-limY);
    }

    // ------------------------------------------------------------------
    // API pública para VentanaControl
    // ------------------------------------------------------------------

    /** Nodo raíz listo para insertar en el layout de VentanaControl. */
    public StackPane getContenedor() { return contenedorMapa; }

    /** Canvas expuesto para que VentanaControl registre el click de selección manual. */
    public Canvas getCanvas() { return canvasMap; }

    /**
     * Registra el handler de clic sobre el canvas.
     * VentanaControl lo usa para el modo selección manual de pasajero.
     */
    public void setOnMapaClicked(javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        // Cambiamos Clicked por Released. 
        // Ahora es imposible que JavaFX ignore el evento, incluso si arrastrás el mouse.
        canvasMap.setOnMouseReleased(handler);
    }

    /**
     * Registra un callback que se dispara cuando el usuario inicia un arrastre.
     * VentanaControl lo usa para desactivar el modo seguimiento de cámara.
     */
    public void setOnDragIniciado(Runnable callback) {
        this.onDragIniciado = callback;
    }

    /** Limpia el canvas y redibuja el mapa base. */
    public void limpiar() {
        graphCx.clearRect(0, 0, widWindow, heightWindow);
        renderizarMapa();
    }

    /** Dibuja el mapa base desde el GeoJSON. */
    public void renderizarMapa() {
        new LectorJSON().dibujarMapa(
            "data/Mapas de Salta-20260602/CentroyMacroSALTA.geojson",
            graphCx, widWindow, heightWindow
        );
    }

    /**
     * Dibuja vehículos, rutas y pasajeros sobre el canvas.
     *
     * <p>Capas de dibujo (orden importa para que nada tape lo anterior):</p>
     * <ol>
     *   <li>Rutas (líneas cian) de vehículos OCUPADO/ENCAMINO</li>
     *   <li>Punto dorado en destino final de cada ruta</li>
     *   <li>Pasajeros — blanco si esperan, dorado si ya tienen taxi confirmado</li>
     *   <li>Círculos de vehículos — verde=disponible, rojo=ocupado</li>
     * </ol>
     */
    public void dibujarVehiculosYRutas(ArrayList<Vehiculo> flota,
                                        GrafoSalta grafo,
                                        Despachador despachador) {
        // --- CAPA 1: rutas ---
        graphCx.setLineWidth(3.5);
        graphCx.setStroke(Color.web("#00E5FF", 0.5));

        for (Vehiculo v : flota) {
            if (v.getState() != EstadoVehiculo.OCUPADO &&
                v.getState() != EstadoVehiculo.ENCAMINO) continue;

            ArrayList<Integer> ruta = v.getRutaAsignada();
            if (ruta == null || ruta.isEmpty()) continue;

            graphCx.beginPath();
            graphCx.moveTo(lngAPixel(v.getLngDecimal()), latAPixel(v.getLatDecimal()));

            int idAnterior = v.getNodoActual();
            for (Integer idNodo : ruta) {
                NodoMapa nActual = grafo.getNodo(idNodo);
                if (nActual != null && estaDentro(nActual)) {
                    double x = lngAPixel(nActual.getLongitud());
                    double y = latAPixel(nActual.getLatitud());
                    if (grafo.getMatrizCosto().devolver(idAnterior, idNodo) != null)
                        graphCx.lineTo(x, y);
                    else
                        graphCx.moveTo(x, y);
                    idAnterior = idNodo;
                }
            }
            graphCx.stroke();

            // --- CAPA 2: destino dorado ---
            NodoMapa dest = grafo.getNodo(ruta.get(ruta.size() - 1));
            if (dest != null && estaDentro(dest)) {
                graphCx.setFill(Color.web("#FFD700",0.001));
                graphCx.fillOval(lngAPixel(dest.getLongitud()) - 6,
                                 latAPixel(dest.getLatitud())  - 6, 12, 12);
            }
        }

        // --- CAPA 3: pasajeros (una sola vez, fuera del loop) ---
        for (NodoMapa p : despachador.getPasajerosEsperando()) {
            if (p == null || !estaDentro(p)) continue;
            
            double x = lngAPixel(p.getLongitud());
            double y = latAPixel(p.getLatitud());
            
            // Dibujamos un "halo" de color detrás del tipito para no perder
            // la lógica visual de si está esperando (blanco) o confirmado (dorado)
            graphCx.setFill(p.isConfirmado() ? Color.web("#FFD700", 0.001) : Color.web("#FFFFFF", 0.1));
            graphCx.fillOval(x - 9, y - 9, 18, 18); 

            if (imgPasajero != null) {
                // Si la imagen cargó bien, la dibujamos de 16x16 píxeles centrada
                double size = 16.0;
                graphCx.drawImage(imgPasajero, x - (size/2), y - (size/2), size, size);
            } else {
                // Fallback de seguridad: si no encuentra el PNG, dibuja el punto original
                graphCx.setFill(p.isConfirmado() ? Color.web("#FFD700") : Color.web("#FFFFFF"));
                graphCx.fillOval(x - 5, y - 5, 10, 10);
            }
        }

        // --- CAPA 4: vehículos ---
        for (Vehiculo v : flota) {
            double x, y;
            if (v.getState() == EstadoVehiculo.OCUPADO ||
                v.getState() == EstadoVehiculo.ENCAMINO) {
                x = lngAPixel(v.getLngDecimal());
                y = latAPixel(v.getLatDecimal());
            } else {
                NodoMapa nodo = grafo.getNodo(v.getNodoActual());
                if (nodo == null) continue;
                x = lngAPixel(nodo.getLongitud());
                y = latAPixel(nodo.getLatitud());
            }
            graphCx.setFill(v.getState() == EstadoVehiculo.DISPONIBLE
                ? Color.web("#39FF14")
                : Color.web("#FF003C"));
            graphCx.fillOval(x - 6, y - 6, 12, 12);
        }
    }

    /**
     * Centra la cámara sobre una posición geográfica (modo tracking).
     * Solo actúa si hay zoom aplicado.
     */
    public void centrarCamara(double lat, double lng) {
        if (canvasMap.getScaleX() <= 1.0) return;

        double xPixel = lngAPixel(lng);
        double yPixel = latAPixel(lat);
        double tx = (widWindow  / 2.0) - xPixel;
        double ty = (heightWindow / 2.0) - yPixel;

        double limX = (widWindow    * (canvasMap.getScaleX() - 1.0)) / 2.0;
        double limY = (heightWindow * (canvasMap.getScaleY() - 1.0)) / 2.0;

        canvasMap.setTranslateX(Math.max(-limX, Math.min(limX, tx)));
        canvasMap.setTranslateY(Math.max(-limY, Math.min(limY, ty)));
    }

    // ------------------------------------------------------------------
    // Conversión de coordenadas (público para que VentanaControl las use
    // en el click de selección manual)
    // ------------------------------------------------------------------

    public double lngAPixel(double lng) {
        return (lng - LectorJSON.LNG_MIN) / (LectorJSON.LNG_MAX - LectorJSON.LNG_MIN) * widWindow;
    }

    public double latAPixel(double lat) {
        return (1.0 - (lat - LectorJSON.LAT_MIN) / (LectorJSON.LAT_MAX - LectorJSON.LAT_MIN)) * heightWindow;
    }

    public double pixelALng(double x) {
        return LectorJSON.LNG_MIN + (x / widWindow)  * (LectorJSON.LNG_MAX - LectorJSON.LNG_MIN);
    }

    public double pixelALat(double y) {
        return LectorJSON.LAT_MIN + (1.0 - (y / heightWindow)) * (LectorJSON.LAT_MAX - LectorJSON.LAT_MIN);
    }

    private boolean estaDentro(NodoMapa nodo) {
        return nodo.getLatitud()  >= LectorJSON.LAT_MIN &&
               nodo.getLatitud()  <= LectorJSON.LAT_MAX &&
               nodo.getLongitud() >= LectorJSON.LNG_MIN &&
               nodo.getLongitud() <= LectorJSON.LNG_MAX;
    }
}