package Modelo.grafoDirigido;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import Modelo.contenedores.ListaDoubleLinkedL;
import Modelo.contenedores.MatrizGrafo;
import Modelo.recursos.NodoMapa;

public class GrafoSalta extends AbsGrafoD {
    protected NodoMapa [] catalogo;
    protected long [] idsOsm;
    protected String rutaMetaDatos;
    protected String rutaMatriz;

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
    public GrafoSalta(String rutaMD, String rutaMTZ){
        super(contarLineas(rutaMD));
        this.rutaMetaDatos=rutaMD;
        this.rutaMatriz=rutaMTZ;
        int n=getOrden();
        this.idsOsm = new long[n];
        this.catalogo= new NodoMapa[n];
    }
    @Override
    public void cargarGrafo(){
        cargarMetadatos();
        cargarAristas();
    }
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
    public int buscarIndice(long idOsM) {
        for (int i = 0; i < this.idsOsm.length; i++) {
            if (this.idsOsm[i] == idOsM) {
                return i;
            }
        }
        return -1;
    }
    public NodoMapa getNodo(int indice) {
        if (indice >= 0 && indice < this.catalogo.length) {
            return this.catalogo[indice];
        }
        return null;
    }
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
public int actualizarPeso(long idOrigen, long idDestino, String tipoVia) {
    int indiceU = buscarIndice(idOrigen);
    int indiceV = buscarIndice(idDestino);
    if (indiceU == -1 || indiceV == -1) return 0;
    if (this.matrizCosto.devolver(indiceU, indiceV) == null) return 0;
    double eta = catalogo[indiceU].calcularETA(catalogo[indiceV], tipoVia);
    this.matrizCosto.actualizar(eta, indiceU, indiceV);
    return 1;
}
public ArrayList<Integer> recuperarCaminoDijkstra(int origen, int destino) {
        ArrayList<Integer> ruta = new ArrayList<>();
        int actual = destino;
        while (actual != origen && actual != -1) {
            ruta.add(0, actual); 
            Object previoObj = this.listaCamino.devolver(actual);
            if (previoObj == null) break; 
            actual = (int) previoObj;
        }
        ruta.add(0, origen); 
        return ruta;
    }
    public ArrayList<Integer> recuperarCaminoFloyd(int origen, int destino) {
        ArrayList<Integer> ruta = new ArrayList<>();
        ruta.add(origen);
        construirRutaFloyd(origen, destino, ruta);
        if (origen != destino) {
            ruta.add(destino);
        }
        return ruta;
    }
    private void construirRutaFloyd(int i, int j, ArrayList<Integer> ruta) {
        Object kObj = this.matrizCaminoF.devolver(i, j);
        if (kObj != null) {
            int k = (int) kObj;
            construirRutaFloyd(i, k, ruta);
            ruta.add(k);
            construirRutaFloyd(k, j, ruta);
        }
    }
}

