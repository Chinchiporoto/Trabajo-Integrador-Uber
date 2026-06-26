package Modelo.grafoDirigido;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import Modelo.contenedores.MatrizGrafo;
import Modelo.recursos.NodoMapa;
import Modelo.recursos.RutaAsignada;

/**
 * Grafo dirigido que representa la red de calles de la ciudad de Salta.
 *
 * <p>
 * Especialización de {@link AbsGrafoD} que carga datos geográficos desde
 * archivos CSV:
 * metadatos de nodos (intersecciones) y una matriz de adyacencia que define las
 * aristas (calles).
 * Implementa algoritmos de camino más corto (Dijkstra y Floyd-Warshall) para
 * cálculo de rutas.
 * </p>
 *
 * @author Proyecto AYED
 * @version 1.1 (optimizado)
 * @see AbsGrafoD
 * @see NodoMapa
 */
public class GrafoSalta extends AbsGrafoD {

    protected NodoMapa[] catalogo;
    protected long[] idsOsm;

    protected String rutaMetaDatos;
    protected String rutaMatriz;

    private HashMap<Long, Integer> idToIndex;

    private double[][] floydCostoRapido;
    private int[][] floydCaminoRapido; // -1 = sin nodo intermedio

    public volatile int floydK = 0;

    private int[][] vecinosCache;

    // ----------------------------------------------------------------

    public static int contarLineas(String ruta) {
        int lineas = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            while (br.readLine() != null)
                lineas++;
        } catch (IOException e) {
            System.out.println("Error contando líneas en " + ruta + ": " + e.getMessage());
        }
        return (lineas > 0) ? lineas - 1 : 0;
    }

    public GrafoSalta(String rutaMD, String rutaMTZ) {
        super(contarLineas(rutaMD));
        this.rutaMetaDatos = rutaMD;
        this.rutaMatriz = rutaMTZ;
        int n = getOrden();
        this.idsOsm = new long[n];
        this.catalogo = new NodoMapa[n];
        this.idToIndex = new HashMap<>(n * 2); // capacidad inicial generosa para evitar rehashing
    }

    @Override
    public void cargarGrafo() {
        cargarMetadatos();
        cargarAristas();
        buildVecinosCache();
        obtenerCostoFloyd(0, 0);// OPTIMIZACIÓN 4: pre-calcular vecinos una sola vez
    }

    private void cargarMetadatos() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.rutaMetaDatos))) {
            String linea;
            br.readLine(); // saltar encabezado
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

                // OPTIMIZACIÓN 1: poblar el mapa al mismo tiempo que cargamos
                this.idToIndex.put(id, i);

                i++;
            }
        } catch (Exception e) {
            System.out.println("Error al cargar los metadatos (Nodos): " + e.getMessage());
        }
    }

    private void cargarAristas() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.rutaMatriz))) {
            String[] cols = br.readLine().split(",");
            long[] idsColumnas = new long[cols.length - 1];
            for (int j = 1; j < cols.length; j++)
                idsColumnas[j - 1] = Long.parseLong(cols[j].trim());

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                // OPTIMIZACIÓN 1: buscarIndice ahora es O(1)
                int indiceU = buscarIndice(Long.parseLong(datos[0].trim()));
                if (indiceU == -1)
                    continue;
                for (int j = 1; j < datos.length; j++) {
                    if (datos[j].trim().equals("1")) {
                        int indiceV = buscarIndice(idsColumnas[j - 1]);
                        if (indiceV == -1)
                            continue;
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
     * OPTIMIZACIÓN 1: lookup O(1) con HashMap en lugar de búsqueda lineal O(n).
     */
    public int buscarIndice(long idOsM) {
        if (idToIndex != null) {
            Integer idx = idToIndex.get(idOsM);
            return (idx != null) ? idx : -1;
        }
        // Fallback por seguridad (no debería llegar acá)
        for (int i = 0; i < this.idsOsm.length; i++) {
            if (this.idsOsm[i] == idOsM)
                return i;
        }
        return -1;
    }

    public NodoMapa getNodo(int indice) {
        if (indice >= 0 && indice < this.catalogo.length)
            return this.catalogo[indice];
        return null;
    }

    public int[] obtenerCaminoDijkstra(int origen, int destino) {
        int n = this.ordenGrafo;
        double[] dist = new double[n];
        int[] camino = new int[n];
        boolean[] enSolucion = new boolean[n];

        for (int i = 0; i < n; i++) {
            dist[i] = infinito;
            camino[i] = -1;
            enSolucion[i] = false;
        }

        dist[origen] = 0.0;
        enSolucion[origen] = true;

        for (int i = 0; i < n; i++) {
            if (i != origen) {
                Object val = this.matrizCosto.devolver(origen, i);
                if (val != null) {
                    dist[i] = (double) val;
                    camino[i] = origen;
                }
            }
        }

        for (int iter = 1; iter < n; iter++) {
            double minCost = infinito;
            int minVertex = -1;
            for (int w = 0; w < n; w++) {
                if (!enSolucion[w] && dist[w] < minCost) {
                    minCost = dist[w];
                    minVertex = w;
                }
            }
            if (minVertex == -1)
                break;
            enSolucion[minVertex] = true;
            for (int v = 0; v < n; v++) {
                if (!enSolucion[v]) {
                    Object arc = this.matrizCosto.devolver(minVertex, v);
                    if (arc != null) {
                        double nuevaDist = minCost + (double) arc;
                        if (nuevaDist < dist[v]) {
                            dist[v] = nuevaDist;
                            camino[v] = minVertex;
                        }
                    }
                }
            }
        }
        return camino; // ← local, sin campo compartido
    }

    /**
     * Recupera la ruta calculada por Dijkstra.
     * OPTIMIZACIÓN 3: lee del int[] en lugar de ListaDoubleLinkedL.
     * OPTIMIZACIÓN 5: agrega al final (O(1)) y luego invierte,
     * en vez de insertar al frente (O(n)) en cada paso.
     */
    public ArrayList<Integer> recuperarCaminoDijkstra(int[] camino, int origen, int destino) {
        ArrayList<Integer> ruta = new ArrayList<>();
        if (camino == null)
            return ruta;
        int actual = destino;
        while (actual != origen && actual != -1) {
            ruta.add(actual);
            actual = camino[actual];
        }
        int izq = 0, der = ruta.size() - 1;
        while (izq < der) {
            int tmp = ruta.get(izq);
            ruta.set(izq, ruta.get(der));
            ruta.set(der, tmp);
            izq++;
            der--;
        }
        return ruta;
    }

    @Override
    public double obtenerCostoFloyd(int origen, int destino) {
        if (this.floydCostoRapido == null) {
            int n = this.ordenGrafo;
            double[][] dist = new double[n][n];
            int[][] next = new int[n][n];

            // Inicializar matrices
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == j) {
                        dist[i][j] = 0.0;
                        next[i][j] = -1;
                    } else {
                        Object val = this.matrizCosto.devolver(i, j);
                        dist[i][j] = (val != null) ? (double) val : infinito;
                        next[i][j] = -1;
                    }
                }
            }

            for (int k = 0; k < n; k++) {
                this.floydK = k;
                for (int i = 0; i < n; i++) {
                    double ik = dist[i][k];
                    if (ik >= infinito)
                        continue;
                    for (int j = 0; j < n; j++) {
                        double nuevaDist = ik + dist[k][j];
                        if (nuevaDist < dist[i][j]) {
                            dist[i][j] = nuevaDist;
                            next[i][j] = k;
                        }
                    }
                }
            }

            this.floydCostoRapido = dist;
            this.floydCaminoRapido = next;
            this.matrizCostoF = new MatrizGrafo(n);
            this.matrizCaminoF = new MatrizGrafo(n);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    this.matrizCostoF.actualizar(dist[i][j], i, j);
                    if (next[i][j] != -1)
                        this.matrizCaminoF.actualizar(next[i][j], i, j);
                }
            }
        }

        return this.floydCostoRapido[origen][destino];
    }

    public ArrayList<Integer> recuperarCaminoFloyd(int origen, int destino) {
        ArrayList<Integer> ruta = new ArrayList<>();
        if (floydCostoRapido == null || floydCostoRapido[origen][destino] >= infinito)
            return ruta;
        construirRutaFloydRapido(origen, destino, ruta);
        if (origen != destino)
            ruta.add(destino);
        return ruta;
    }

    private void construirRutaFloydRapido(int i, int j, ArrayList<Integer> ruta) {
        int k = floydCaminoRapido[i][j];
        if (k != -1) {
            construirRutaFloydRapido(i, k, ruta);
            ruta.add(k);
            construirRutaFloydRapido(k, j, ruta);
        }
    }

    public int actualizarPeso(long idOrigen, long idDestino, String tipoVia) {
        int indiceU = buscarIndice(idOrigen);
        int indiceV = buscarIndice(idDestino);
        if (indiceU == -1 || indiceV == -1)
            return 0;
        if (this.matrizCosto.devolver(indiceU, indiceV) == null)
            return 0;
        double eta = catalogo[indiceU].calcularETA(catalogo[indiceV], tipoVia);
        this.matrizCosto.actualizar(eta, indiceU, indiceV);
        return 1;
    }

    private void buildVecinosCache() {
        int n = this.getOrden();
        this.vecinosCache = new int[n][];

        for (int i = 0; i < n; i++) {
            int count = 0;
            for (int j = 0; j < n; j++) {
                Object c = this.matrizCosto.devolver(i, j);
                if (c != null) {
                    double costo = (double) c;
                    if (costo > 0.0 && costo < infinito)
                        count++;
                }
            }

            this.vecinosCache[i] = new int[count];
            int idx = 0;
            for (int j = 0; j < n; j++) {
                Object c = this.matrizCosto.devolver(i, j);
                if (c != null) {
                    double costo = (double) c;
                    if (costo > 0.0 && costo < infinito)
                        this.vecinosCache[i][idx++] = j;
                }
            }
        }
    }

    public ArrayList<Integer> obtenerVecinosValidos(int nodoOrigen) {
        ArrayList<Integer> vecinos = new ArrayList<>();
        if (vecinosCache != null && nodoOrigen >= 0 && nodoOrigen < vecinosCache.length) {
            int[] cache = vecinosCache[nodoOrigen];
            for (int i = 0; i < cache.length; i++)
                vecinos.add(cache[i]);
            return vecinos;
        }
        int n = this.getOrden();
        for (int dest = 0; dest < n; dest++) {
            Object costoObj = this.matrizCosto.devolver(nodoOrigen, dest);
            if (costoObj != null) {
                double costo = (double) costoObj;
                if (costo > 0.0 && costo < infinito)
                    vecinos.add(dest);
            }
        }
        return vecinos;
    }

    public boolean esPuntoMuerto(int nodo) {
        if (vecinosCache != null && nodo >= 0 && nodo < vecinosCache.length)
            return vecinosCache[nodo].length == 0;
        return obtenerVecinosValidos(nodo).isEmpty();
    }

    @Override
    public void muestraGrafo() {
        for (int i = 0; i < getOrden(); i++) {
            for (int j = 0; j < getOrden(); j++) {
                if (i != j) {
                    Object val = this.matrizCosto.devolver(i, j);
                    if (val != null) {
                        double cost = (double) val;
                        if (cost != infinito)
                            System.out.println("costo " + i + " a " + j + " -> " + cost);
                    }
                }
            }
        }
    }

    public int buscarNodoMasCercano(double lat, double lng) {
        int nodoCercano = -1;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < getOrden(); i++) {
            NodoMapa n = getNodo(i);
            if (n != null && !esPuntoMuerto(i)) {
                double dLat = n.getLatitud() - lat;
                double dLng = n.getLongitud() - lng;
                double distSq = dLat * dLat + dLng * dLng;
                if (distSq < minDist) {
                    minDist = distSq;
                    nodoCercano = i;
                }
            }
        }
        return nodoCercano;
    }

    public RutaAsignada dijkstraCompleto(int origen, int destino) {
        int n = this.ordenGrafo;
        double[] dist = new double[n];
        int[] camino = new int[n];
        boolean[] enSolucion = new boolean[n];

        for (int i = 0; i < n; i++) {
            dist[i] = infinito;
            camino[i] = -1;
            enSolucion[i] = false;
        }
        dist[origen] = 0.0;
        enSolucion[origen] = true;

        for (int i = 0; i < n; i++) {
            if (i != origen) {
                Object val = this.matrizCosto.devolver(origen, i);
                if (val != null) {
                    dist[i] = (double) val;
                    camino[i] = origen;
                }
            }
        }

        for (int iter = 1; iter < n; iter++) {
            double minCost = infinito;
            int minVertex = -1;
            for (int w = 0; w < n; w++) {
                if (!enSolucion[w] && dist[w] < minCost) {
                    minCost = dist[w];
                    minVertex = w;
                }
            }
            if (minVertex == -1)
                break;
            enSolucion[minVertex] = true;
            for (int v = 0; v < n; v++) {
                if (!enSolucion[v]) {
                    Object arc = this.matrizCosto.devolver(minVertex, v);
                    if (arc != null) {
                        double nuevaDist = minCost + (double) arc;
                        if (nuevaDist < dist[v]) {
                            dist[v] = nuevaDist;
                            camino[v] = minVertex;
                        }
                    }
                }
            }
        }

        // Reconstruir camino
        ArrayList<Integer> ruta = new ArrayList<>();
        int actual = destino;
        while (actual != origen && actual != -1) {
            ruta.add(actual);
            actual = camino[actual];
        }
        int izq = 0, der = ruta.size() - 1;
        while (izq < der) {
            int tmp = ruta.get(izq);
            ruta.set(izq, ruta.get(der));
            ruta.set(der, tmp);
            izq++;
            der--;
        }

        return new RutaAsignada(dist[destino], ruta);
    }
}
