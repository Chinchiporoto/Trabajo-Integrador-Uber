package servicio;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import grafoDirigido.GrafoSalta;

public class LectorJSON {

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
}