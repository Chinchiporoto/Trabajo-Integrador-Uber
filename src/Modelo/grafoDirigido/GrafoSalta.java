package Modelo.grafoDirigido;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import Modelo.contenedores.ListaDoubleLinkedL;
import Modelo.contenedores.MatrizGrafo;
import Modelo.recursos.NodoMapa;

/**
 * Grafo dirigido que representa la red de calles de la ciudad de Salta.
 * 
 * <p>Especialización de {@link AbsGrafoD} que carga datos geográficos desde archivos CSV:
 * metadatos de nodos (intersecciones) y una matriz de adyacencia que define las aristas (calles).
 * Implementa algoritmos de camino más corto (Dijkstra y Floyd-Warshall) para cálculo de rutas.
 * </p>
 * 
 * <p>Características principales:</p>
 * <ul>
 *   <li>Carga de nodos con coordenadas reales (latitud/longitud) de OSM</li>
 *   <li>Cálculo de ETA usando distancia Haversine y velocidades por tipo de vía</li>
 *   <li>Algoritmos de ruta más corta adaptables según distancia</li>
 *   <li>Detección de puntos muertos (nodos sin salida)</li>
 *   <li>Patrullaje de vecinos válidos para navegación</li>
 * </ul>
 * 
 * @author Proyecto AYED
 * @version 1.0
 * @see AbsGrafoD
 * @see NodoMapa
 */
public class GrafoSalta extends AbsGrafoD {
    /** Array que almacena los nodos (intersecciones) del grafo con su información geográfica */
    protected NodoMapa [] catalogo;
    /** Array con los identificadores OSM (OpenStreetMap) de cada nodo */
    protected long [] idsOsm;
    /** Ruta al archivo CSV de metadatos de nodos (latitud, longitud, nombres de calles) */
    protected String rutaMetaDatos;
    /** Ruta al archivo CSV con la matriz de adyacencia del grafo */
    protected String rutaMatriz;

    /**
     * Cuenta el número de líneas en un archivo CSV.
     * 
     * <p>Utilizado para determinar el orden (número de nodos) del grafo
     * al leer el archivo de metadatos.</p>
     * 
     * @param ruta ruta del archivo a contar
     * @return número de líneas (excluyendo encabezado)
     */
    public static int contarLineas(String ruta){
    int lineas=0;
    try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
        while (br.readLine() != null) {
            lineas++;
        }
    } 
    catch (IOException e) {
        System.out.println("Error contando líneas en " + ruta + ": " + e.getMessage());
    }
    return (lineas > 0) ? lineas - 1 : 0;
    }
    /**
     * Constructor que inicializa el grafo de Salta con las rutas a los archivos de datos.
     * 
     * <p>Determina el orden del grafo contando líneas en el archivo de metadatos.
     * Inicializa los arrays de nodos e IDs de OSM.</p>
     * 
     * @param rutaMD ruta del archivo CSV de metadatos de nodos
     * @param rutaMTZ ruta del archivo CSV de matriz de adyacencia
     */
    public GrafoSalta(String rutaMD, String rutaMTZ){
        super(contarLineas(rutaMD));
        this.rutaMetaDatos=rutaMD;
        this.rutaMatriz=rutaMTZ;
        int n=getOrden();
        this.idsOsm = new long[n];
        this.catalogo= new NodoMapa[n];
    }
    /**
     * Carga el grafo completo desde los archivos de datos.
     * 
     * <p>Invoca secuencialmente la carga de metadatos (nodos) y aristas.</p>
     */
    @Override
    public void cargarGrafo(){
        cargarMetadatos();
        cargarAristas();
    }
    /**
     * Carga los metadatos de los nodos desde el archivo CSV.
     * 
     * <p>Lee cada línea del archivo y crea un objeto {@link NodoMapa} con:</p>
     * <ul>
     *   <li>ID de OSM (identificador único)</li>
     *   <li>Coordenadas geográficas (latitud, longitud)</li>
     *   <li>Nombres de las dos calles que forman la intersección</li>
     *   <li>Nombre de la esquina</li>
     * </ul>
     * 
     * @see NodoMapa
     */
    private void cargarMetadatos() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.rutaMetaDatos))) {
            String linea;
            br.readLine();
            int i = 0;
            while ((linea = br.readLine()) != null && i < getOrden()) {
                String[] datos = linea.split(",");
                long id = Long.parseLong(datos[0].trim());
                double lat = Double.parseDouble(datos[1].trim());
                double lng = Double.parseDouble(datos[2].trim());
                String calleA = datos[3].trim();
                String calleB = datos[4].trim();
                String esquina = datos[5].trim();
                NodoMapa nodo = new NodoMapa(id, lat, lng, calleA, calleB, esquina);
                this.catalogo[i] = nodo;
                this.idsOsm[i] = id;
                i++;
            }
        } catch (Exception e) {
            System.out.println("Error al cargar los metadatos (Nodos): " + e.getMessage());
        }
    }
    /**
     * Carga las aristas desde el archivo de matriz de adyacencia.
     * 
     * <p>Lee la matriz binaria (1 = existe arista, 0 = no existe) e inicializa
     * los pesos de cada arista basándose en la distancia Haversine y el tipo de vía.</p>
     * 
     * <p>Para cada arista, calcula el ETA usando {@link NodoMapa#calcularETA}.</p>
     * 
     * @see NodoMapa#calcularETA(NodoMapa, String)
     */
    private void cargarAristas() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.rutaMatriz))) {
               String[] cols = br.readLine().split(",");
        long[] idsColumnas = new long[cols.length - 1];
        for (int j = 1; j < cols.length; j++)
            idsColumnas[j-1] = Long.parseLong(cols[j].trim());
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] datos = linea.split(",");
            int indiceU = buscarIndice(Long.parseLong(datos[0].trim()));
            if (indiceU == -1) continue;
            for (int j = 1; j < datos.length; j++) {
                if (datos[j].trim().equals("1")) {
                    int indiceV = buscarIndice(idsColumnas[j-1]);
                    if (indiceV == -1) continue;
                    double eta = catalogo[indiceU].calcularETA(catalogo[indiceV], "residential");
                    this.matrizCosto.actualizar(eta, indiceU, indiceV);
                }
            }
        }
        } catch (Exception e) {
            System.out.println("Error al cargar las aristas: " + e.getMessage());
        }
    }
    /**
     * Busca el índice de un nodo usando su ID de OSM.
     * 
     * @param idOsM el identificador de OSM del nodo
     * @return el índice del nodo en el catálogo, o -1 si no existe
     */
    public int buscarIndice(long idOsM) {
        for (int i = 0; i < this.idsOsm.length; i++) {
            if (this.idsOsm[i] == idOsM) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Obtiene un nodo del catálogo por su índice.
     * 
     * @param indice el índice del nodo en el grafo
     * @return el {@link NodoMapa} en ese índice, o null si el índice es inválido
     */
    public NodoMapa getNodo(int indice) {
        if (indice >= 0 && indice < this.catalogo.length) {
            return this.catalogo[indice];
        }
        return null;
    }
    /**
     * Calcula el costo (distancia/tiempo) entre dos nodos usando el algoritmo Dijkstra.
     * 
     * <p>Implementa el algoritmo clásico de Dijkstra para encontrar el camino más corto
     * desde el origen a todos los demás nodos. Mantiene listas de distancias, caminos y soluciones.</p>
     * 
     * <p><b>Complejidad:</b> O(n²) donde n es el número de nodos.</p>
     * 
     * @param origen índice del nodo origen
     * @param destino índice del nodo destino
     * @return el costo total del camino más corto (infinito si no existe ruta)
     * 
     * @see #recuperarCaminoDijkstra(int, int)
     */
    @Override
public double obtenerCostoDijkstra(int origen, int destino) {
    // inicializar listaDistancia con infinito explícito
    this.listaDistancia = new ListaDoubleLinkedL();
    this.listaCamino    = new ListaDoubleLinkedL();
    this.listaSolucion  = new ListaDoubleLinkedL();

    for (int i = 0; i < getOrden(); i++) {
        this.listaSolucion.insertar(-1, i);
        this.listaCamino.insertar(-1, i);
        this.listaDistancia.insertar(infinito, i);
    }
    this.listaSolucion.reemplazar(origen, origen);

    for (int i = 0; i < getOrden(); i++) {
        if (i != origen) {
            Object val = this.matrizCosto.devolver(origen, i);
            this.listaDistancia.reemplazar(val != null ? val : infinito, i);
            this.listaCamino.reemplazar(origen, i);
        }
    }
    for (int i = 1; i < getOrden(); i++) {
        double minCost = infinito;
        int minVertex = -1;

        for (int w = 0; w < getOrden(); w++) {
            if (w != origen) {
                double currCost = (double) this.listaDistancia.devolver(w);
                int vertex      = (int)    this.listaSolucion.devolver(w);
                if (currCost < minCost && vertex == -1) {
                    minCost = currCost;
                    minVertex = w;
                }
            }
        }
        if (minVertex != -1) {
            this.listaSolucion.reemplazar(minVertex, minVertex);
            this.listaDistancia.reemplazar(minCost, minVertex);

            for (int v = 0; v < getOrden(); v++) {
                int vertex = (int) this.listaSolucion.devolver(v);
                if (vertex == -1) {
                    Object arc   = this.matrizCosto.devolver(minVertex, v);
                    double arcCost  = (arc != null) ? (double) arc : infinito;
                    double currCost = (double) this.listaDistancia.devolver(v);
                    if (minCost + arcCost < currCost) {
                        this.listaDistancia.reemplazar(minCost + arcCost, v);
                        this.listaCamino.reemplazar(minVertex, v);
                    }
                }
            }
        }
    }

    return (double) this.listaDistancia.devolver(destino);
}

/**
 * Calcula el costo entre dos nodos usando el algoritmo Floyd-Warshall.
 * 
 * <p>Implementa Floyd-Warshall para encontrar caminos más cortos entre todos los pares de nodos.
 * Se ejecuta una sola vez y cachea el resultado para futuras consultas.</p>
 * 
 * <p><b>Complejidad:</b> O(n³) en la primera ejecución, O(1) en consultas posteriores.</p>
 * 
 * @param origen índice del nodo origen
 * @param destino índice del nodo destino
 * @return el costo total del camino más corto (infinito si no existe ruta)
 * 
 * @see #recuperarCaminoFloyd(int, int)
 */
@Override
public double obtenerCostoFloyd(int origen, int destino) {
    if (this.matrizCostoF == null) {
        // inicializar con infinito antes de correr Floyd
        this.matrizCostoF  = new MatrizGrafo(this.ordenGrafo);
        this.matrizCaminoF = new MatrizGrafo(this.ordenGrafo);

        for (int i = 0; i < ordenGrafo; i++) {
            for (int j = 0; j < ordenGrafo; j++) {
                if (i == j) {
                    matrizCostoF.actualizar(0.0, i, j);
                } else {
                    Object val = matrizCosto.devolver(i, j);
                    matrizCostoF.actualizar(val != null ? val : infinito, i, j);
                }
            }
        }

        // Floyd
        for (int k = 0; k < ordenGrafo; k++) {
            for (int i = 0; i < ordenGrafo; i++) {
                for (int j = 0; j < ordenGrafo; j++) {
                    double ik = ((Double) matrizCostoF.devolver(i, k));
                    double kj = ((Double) matrizCostoF.devolver(k, j));
                    double ij = ((Double) matrizCostoF.devolver(i, j));
                    if (ik + kj < ij) {
                        matrizCostoF.actualizar(ik + kj, i, j);
                        matrizCaminoF.actualizar(k, i, j);
                    }
                }
            }
        }
    }
    return ((Double) this.matrizCostoF.devolver(origen, destino));
}

/**
 * Actualiza el peso de una arista específica basado en el tipo de vía.
 * 
 * <p>Recalcula el ETA de una arista usando la distancia Haversine y el tipo de vía especificado.</p>
 * 
 * @param idOrigen ID de OSM del nodo origen
 * @param idDestino ID de OSM del nodo destino
 * @param tipoVia tipo de vía para el cálculo de velocidad (ej: "residential", "primary")
 * @return 1 si la actualización fue exitosa, 0 si los nodos o arista no existen
 * 
 * @see NodoMapa#calcularETA(NodoMapa, String)
 */
public int actualizarPeso(long idOrigen, long idDestino, String tipoVia) {
    int indiceU = buscarIndice(idOrigen);
    int indiceV = buscarIndice(idDestino);
    if (indiceU == -1 || indiceV == -1) return 0;
    if (this.matrizCosto.devolver(indiceU, indiceV) == null) return 0;
    double eta = catalogo[indiceU].calcularETA(catalogo[indiceV], tipoVia);
    this.matrizCosto.actualizar(eta, indiceU, indiceV);
    return 1;
}

/**
 * Recupera la ruta completa (lista de nodos) calculada por Dijkstra.
 * 
 * <p>Reconstruye el camino desde el origen al destino siguiendo los punteros
 * de camino almacenados durante la ejecución de Dijkstra.</p>
 * 
 * @param origen índice del nodo origen
 * @param destino índice del nodo destino
 * @return ArrayList de índices de nodos que forman la ruta
 * 
 * @see #obtenerCostoDijkstra(int, int)
 */
public ArrayList<Integer> recuperarCaminoDijkstra(int origen, int destino) {
        ArrayList<Integer> ruta = new ArrayList<>();
        int actual = destino;
        while (actual != origen && actual != -1) {
            ruta.add(0, actual); 
            Object previoObj = this.listaCamino.devolver(actual);
            if (previoObj == null) break; 
            actual = (int) previoObj;
        }
        return ruta;
    }
    /**
     * Recupera la ruta completa (lista de nodos) calculada por Floyd-Warshall.
     * 
     * <p>Reconstruye recursivamente el camino desde el origen al destino
     * usando la matriz de camino generada por Floyd-Warshall.</p>
     * 
     * @param origen índice del nodo origen
     * @param destino índice del nodo destino
     * @return ArrayList de índices de nodos que forman la ruta
     * 
     * @see #obtenerCostoFloyd(int, int)
     */
    public ArrayList<Integer> recuperarCaminoFloyd(int origen, int destino) {
        ArrayList<Integer> ruta = new ArrayList<>();
        construirRutaFloyd(origen, destino, ruta);
        if (origen != destino) {
            ruta.add(destino);
        }
        return ruta;
    }
    /**
     * Construye recursivamente una ruta entre dos nodos usando matriz de caminos de Floyd.
     * 
     * <p>Método auxiliar llamado por {@link #recuperarCaminoFloyd}. Realiza una búsqueda
     * recursiva en la matriz de caminos para reconstruir el path completo.</p>
     * 
     * @param i índice del nodo inicial en la búsqueda recursiva
     * @param j índice del nodo destino en la búsqueda recursiva
     * @param ruta ArrayList donde se acumulan los nodos del camino
     */
    private void construirRutaFloyd(int i, int j, ArrayList<Integer> ruta) {
        Object kObj = this.matrizCaminoF.devolver(i, j);
        if (kObj != null) {
            int k = (int) kObj;
            construirRutaFloyd(i, k, ruta);
            ruta.add(k);
            construirRutaFloyd(k, j, ruta);
        }
    }
    /**
     * Obtiene todos los vecinos válidos (nodos adyacentes con arista válida) de un nodo.
     * 
     * <p>Busca en la matriz de costos todos los destinos que tienen una arista válida
     * desde el nodo origen (costo > 0 e infinito < costo).</p>
     * 
     * @param nodoOrigen índice del nodo del cual obtener vecinos
     * @return ArrayList de índices de nodos vecinos válidos
     */
    public ArrayList<Integer> obtenerVecinosValidos(int nodoOrigen) {
        ArrayList<Integer> vecinosValidos = new ArrayList<>();
        int maxNodos = this.getOrden(); 
        
        for (int destinoC = 0; destinoC < maxNodos; destinoC++) {
            Object costoObj = this.matrizCosto.devolver(nodoOrigen, destinoC);
            
            if (costoObj != null) {
                double costo = (double) costoObj;
                
                if (costo > 0.0 && costo < infinito) { 
                    vecinosValidos.add(destinoC);
                }
            }
        }
        return vecinosValidos;
    }
    /**
     * Determina si un nodo es un punto muerto (sin salidas válidas).
     * 
     * <p>Un punto muerto es un nodo que no tiene vecinos válidos, lo que significa
     * que un vehículo quedaría atrapado allí. Esto ocurre típicamente en callejones sin salida
     * o en nodos aislados del grafo.</p>
     * 
     * @param nodo índice del nodo a verificar
     * @return true si el nodo es un punto muerto, false si tiene al menos una salida válida
     * 
     * @see #obtenerVecinosValidos(int)
     */
    public boolean esPuntoMuerto(int nodo) {
        return obtenerVecinosValidos(nodo).isEmpty();
    }	
}



