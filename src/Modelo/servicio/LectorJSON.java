
package Modelo.servicio;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import Modelo.grafoDirigido.GrafoSalta;

public class LectorJSON {
private static final double LAT_MIN = -24.805;
private static final double LAT_MAX = -24.765;
private static final double LNG_MIN = -65.430;
private static final double LNG_MAX = -65.395;
    public void probarLecturaNodos(String rutaArchivo, GrafoSalta grafo) {
        try {
            String contenido = new String(Files.readAllBytes(Paths.get(rutaArchivo)));
            JSONObject jsonCompleto = new JSONObject(contenido);
            JSONArray elementos = jsonCompleto.getJSONArray("elements");
            int contadorCalles = 0;

            for (int i = 0; i < elementos.length(); i++) {
                JSONObject item = elementos.getJSONObject(i);
                if (!item.getString("type").equals("way")) continue;

                JSONArray nodes = item.getJSONArray("nodes");
                JSONObject tags = item.optJSONObject("tags");
                String tipoVia = "residential";
                boolean esManoUnica = false;

                if (tags != null) {
                    tipoVia = tags.optString("highway", "residential");
                    esManoUnica = tags.optString("oneway", "no").equals("yes");
                }

                for (int j = 0; j < nodes.length() - 1; j++) {
                    long idOrigen  = nodes.getLong(j);
                    long idDestino = nodes.getLong(j + 1);
                    grafo.actualizarPeso(idOrigen, idDestino, tipoVia);
                    if (!esManoUnica)
                        grafo.actualizarPeso(idDestino, idOrigen, tipoVia);
                    contadorCalles++;
                }
            }
            System.out.println("Pesos actualizados: " + contadorCalles + " segmentos.");
        } catch (IOException e) {
            System.out.println("Error abriendo archivo: " + rutaArchivo);
        } catch (Exception e) {
            System.out.println("Error procesando JSON: " + e.getMessage());
        }
    }
    public void dibujarMapa(String rutaGeoJSON, javafx.scene.canvas.GraphicsContext gc, 
                         double ancho, double alto) {
    try {
        String contenido = new String(Files.readAllBytes(Paths.get(rutaGeoJSON)));
        JSONObject json = new JSONObject(contenido);
        JSONArray features = json.getJSONArray("features");
        gc.setStroke(javafx.scene.paint.Color.web("#555555"));
        gc.setLineWidth(1.0);
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature  = features.getJSONObject(i);
            JSONObject props    = feature.optJSONObject("properties");
            JSONArray  coords   = feature.getJSONObject("geometry")
                                         .getJSONArray("coordinates");
            String highway = props != null ? props.optString("highway", "residential") : "residential";
            switch (highway) {
                case "primary":   gc.setStroke(javafx.scene.paint.Color.web("#FFFFFF")); 
                                  gc.setLineWidth(2.5); break;
                case "secondary": gc.setStroke(javafx.scene.paint.Color.web("#DDDDDD")); 
                                  gc.setLineWidth(2.0); break;
                case "tertiary":  gc.setStroke(javafx.scene.paint.Color.web("#AAAAAA")); 
                                  gc.setLineWidth(1.5); break;
                default:          gc.setStroke(javafx.scene.paint.Color.web("#777777")); 
                                  gc.setLineWidth(1.0); break;
            }
            gc.beginPath();
            for (int j = 0; j < coords.length(); j++) {
                JSONArray punto = coords.getJSONArray(j);
                double lng = punto.getDouble(0);
                double lat = punto.getDouble(1);
                double x = lngAPixel(lng, ancho);
                double y = latAPixel(lat, alto);
                if (j == 0) gc.moveTo(x, y);
                else        gc.lineTo(x, y);
            }
            gc.stroke();
        }
    } catch (Exception e) {
        System.out.println("Error dibujando mapa: " + e.getMessage());
    }
}
private double lngAPixel(double lng, double ancho) {
    return (lng - LNG_MIN) / (LNG_MAX - LNG_MIN) * ancho;
}
private double latAPixel(double lat, double alto) {
    return (1.0 - (lat - LAT_MIN) / (LAT_MAX - LAT_MIN)) * alto;
}
}