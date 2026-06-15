package Modelo.simulador;

import java.util.ArrayList;

import Modelo.recursos.RutaAsignada;
import Modelo.Patrones.DijsktraStrat;
import Modelo.Patrones.FloydStrategy;
import Modelo.Patrones.IntelligenceStrategy;
import Modelo.contenedores.VehiculoPriority;
import Modelo.recursos.NodoMapa;
import Modelo.grafoDirigido.AbsGrafo;
import Modelo.grafoDirigido.GrafoSalta;

public class Despachador {
	
    protected ArrayList<Vehiculo> vehiculos;
    protected VehiculoPriority colaDespacho;
    private AbsGrafo map;
    private IntelligenceStrategy metodoDji;
    private IntelligenceStrategy metodoFlo;
    
    public Despachador(ArrayList<Vehiculo>a, AbsGrafo mapa){
        this.colaDespacho=new VehiculoPriority();
        this.vehiculos=a;
        this.map=mapa;
        this.metodoDji= new DijsktraStrat();
        this.metodoFlo= new FloydStrategy();
    }
    
    public void cargaAutos(Vehiculo a){
        vehiculos.add(a);
    }
    public void limpiar(){
        this.colaDespacho.limpiar();
    }
    public void registrarDisponibles(int nodePasajero){ 
        limpiar();
    for(int i=0;i<this.vehiculos.size();i++)
        if(vehiculos.get(i).getState()== EstadoVehiculo.DISPONIBLE){ 
        NodoMapa nodeAuto = ((GrafoSalta)this.map).getNodo(vehiculos.get(i).getNodoActual());
        NodoMapa nodoPasajero = ((GrafoSalta)this.map).getNodo(nodePasajero);
        if (nodeAuto != null && nodoPasajero != null) {
            IntelligenceStrategy metodo;
            int distancia=Math.abs(vehiculos.get(i).getNodoActual()-nodePasajero);
            if (distancia>5)
                metodo=this.metodoFlo;
            else
                metodo=this.metodoDji;
            RutaAsignada ruta = metodo.calculaETA(this.map, nodeAuto, nodoPasajero);
            vehiculos.get(i).setEta(ruta.getEta());
            vehiculos.get(i).setRutaAsignada(ruta.getCaminoNodos());
                }
            this.colaDespacho.meter(vehiculos.get(i));
        }
    }
    public Vehiculo asignaViaje(int nodoOrigen){
        Vehiculo candidato=null;
        while(!this.colaDespacho.estaVacia()){
            candidato=(Vehiculo)this.colaDespacho.sacar();
            if((candidato).aceptaViaje()){ 
                candidato.setState(EstadoVehiculo.OCUPADO);
                return candidato;
            }
        }
        return null;
    }
    
    public void muestraCoches(){
        for(int i=0;i<this.vehiculos.size();i++)
            System.out.println(this.vehiculos.get(i).toString());
    }
    
    public void crearViajeAleatorio(ArrayList<Vehiculo> flota, GrafoSalta grafo) {
        // 1. Generamos solo el nodo pasajero
        int maxNodos = grafo.getOrden();
        int nodoPasajeroAleatorio = (int) (Math.random() * maxNodos);
        
        System.out.println("\n[Modelo-Despachador] Generando solicitud autónoma:");
        System.out.println(" -> Pasajero en nodo: " + nodoPasajeroAleatorio);

        //2. Se llama al método para asignar a alguno de los vehiculos ya instanciados
        this.asignaViaje(nodoPasajeroAleatorio); 
    }
    
}//fin clase
