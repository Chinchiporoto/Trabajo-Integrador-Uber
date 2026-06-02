package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

public class LectorJSON {

    public void probarLecturaNodos(String rutaArchivo) {
        
        try {
            // 1. Se lee el archivo del mapa
            String contenido = new String(Files.readAllBytes(Paths.get(rutaArchivo)));
            JSONObject jsonCompleto = new JSONObject(contenido);
            
            // 2. Se extrae la lista principal de elementos (Nodos y Vías)
            JSONArray elementos = jsonCompleto.getJSONArray("elements");
            
            System.out.println("Iniciando escaneo del mapa de Salta (Formato Raw JSON)...");
            int contadorNodos = 0;
            
            // 3. Recorremos todo buscando las esquinas
            for (int i = 0; i < elementos.length(); i++) {
                JSONObject item = elementos.getJSONObject(i);
                
                // Si el elemento es un nodo (esquina), sacamos sus datos
                if (item.getString("type").equals("node")) {
                    long id = item.getLong("id");
                    double lat = item.getDouble("lat");
                    double lon = item.getDouble("lon");
                    
                    System.out.println("Esquina - ID: " + id + " | Latitud: " + lat + " | Longitud: " + lon);
                    contadorNodos++;
                }
            }
            
            System.out.println("Se procesaron " + contadorNodos + " intersecciones.");
            
        } catch (IOException e) {
            System.out.println("Error: No se pudo abrir el archivo en la ruta: " + rutaArchivo);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error al leer la estructura JSON.");
            e.printStackTrace();
        }
    }
}