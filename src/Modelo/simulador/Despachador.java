package simulador;

import java.util.ArrayList;

import recursos.RutaAsignada;
import Patrones.DijsktraStrat;
import Patrones.FloydStrategy;
import Patrones.IntelligenceStrategy;
import contenedores.VehiculoPriority;
import recursos.NodoMapa;
import grafoDirigido.AbsGrafo;
import grafoDirigido.GrafoSalta;
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
    public void muestraCovhes(){
        for(int i=0;i<this.vehiculos.size();i++)
            System.out.println(this.vehiculos.get(i).toString());
    }
}
