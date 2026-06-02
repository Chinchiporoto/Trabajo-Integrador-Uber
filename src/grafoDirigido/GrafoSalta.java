package grafoDirigido;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import recursos.NodoMapa;

public class GrafoSalta extends AbsGrafoD {
    protected NodoMapa [] catalogo;
    protected long [] idsOsm;
    protected String rutaMetaDatos;
    protected String rutaMatriz;

    public int contarLineas(String ruta){
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
            String linea;
            br.readLine(); 
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                long idOrigen = Long.parseLong(datos[0].trim());
                long idDestino = Long.parseLong(datos[1].trim());
                String tipoVia = datos[2].trim();
                int indiceU = buscarIndice(idOrigen);
                int indiceV = buscarIndice(idDestino);

                if (indiceU != -1 && indiceV != -1) {
                    NodoMapa origen = catalogo[indiceU];
                    NodoMapa destino = catalogo[indiceV];
                    double pesoETA = origen.calcularETA(destino, tipoVia);
                    this.matrizCosto.actualizar(pesoETA, indiceU, indiceV);
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
}

