package servicio;

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
            
            // 2. Se extrae la lista principal de elementos (esquinas y calles)
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
            
         //Este segundo bloque es el que recorre nuevamente el array elementos pero esta vez buscando las calles y haciendo las conexiones

            System.out.println("Iniciando escaneo de calles");
            int contadorCalles = 0;

            for (int i = 0; i < elementos.length(); i++) {
                JSONObject item = elementos.getJSONObject(i);
                
                // 1. FILTRAR SOLO LAS CALLES
                if (item.getString("type").equals("way")) {
                    
                    // 2. EXTRAER LA LISTA DE NODOS QUE COMPONEN ESTA CALLE
                    JSONArray nodes = item.getJSONArray("nodes");
                    
                    // 3. EXTRAER LAS ETIQUETAS (TAGS) PARA CONOCER LAS REGLAS DE LA CALLE
                    JSONObject tags = item.optJSONObject("tags");
                    String tipoVia = "residencial"; // Valor por defecto si no tiene etiqueta
                    boolean esManoUnica = false;
                    String nombreCalle = "Calle sin nombre";
                    
                    if (tags != null) {
                        tipoVia = tags.optString("highway", "residencial");
                        nombreCalle = tags.optString("name", "Calle sin nombre");
                        
                        // Verificamos si la calle es de un solo sentido
                        if (tags.has("oneway") && tags.getString("oneway").equals("yes")) {
                            esManoUnica = true;
                        }
                    }
                    
                    // 4. CONECTAR LOS NODOS EN FORMA SECUENCIAL
                    // Recorremos hasta length() - 1 porque el último nodo no tiene un "siguiente"
                    for (int j = 0; j < nodes.length() - 1; j++) {
                        long idOrigen = nodes.getLong(j);
                        long idDestino = nodes.getLong(j + 1);
                        
                        // Aquí es donde conectarás las esquinas en tu estructura de Grafo.
                        // Por ejemplo, si tu grafo recibe los IDs y las etiquetas, la lógica se vería así:
                        
                        // miGrafo.conectar(idOrigen, idDestino, tipoVia, nombreCalle);
                        System.out.println("Conectando: " + idOrigen + " -> " + idDestino + " (" + nombreCalle + " - " + tipoVia + ")");
                        
                        // Si NO es mano única, significa que es doble mano. Trazamos el camino inverso.
                        if (!esManoUnica) {
                            // miGrafo.conectar(idDestino, idOrigen, tipoVia, nombreCalle);
                            System.out.println("Conectando (Inverso): " + idDestino + " -> " + idOrigen + " (" + nombreCalle + " - " + tipoVia + ")");
                        }
                        
                        contadorCalles++;
                    }
                }
            }

            System.out.println("¡Éxito! Se registraron " + contadorCalles + " segmentos de calles.");
            
        } catch (IOException e) {
            System.out.println("Error: No se pudo abrir el archivo en la ruta: " + rutaArchivo);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error al leer la estructura JSON.");
            e.printStackTrace();
        }
    }
}