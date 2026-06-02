package simulador;

import java.util.ArrayList;

import contenedores.VehiculoPriority;

public class Despachador {
    protected ArrayList<Vehiculo> vehiculos;
    protected VehiculoPriority colaDespacho;

    public Despachador(ArrayList<Vehiculo>a){
        this.colaDespacho=new VehiculoPriority();
        this.vehiculos=a;
    }
    public void cargaAutos(Vehiculo a){
        vehiculos.add(a);
    }
    public void limpiar(){
        this.colaDespacho.limpiar();
    }
    public void registrarDisponibles(){ 
        limpiar();
    for(int i=0;i<this.vehiculos.size();i++)
        if(vehiculos.get(i).getState()== EstadoVehiculo.DISPONIBLE){ 
            //calcularETA nuevo. . . se va ingresando prioritativamente con el calculo de eta...
            this.colaDespacho.meter(vehiculos.get(i));
        }
    }
    public Vehiculo asignaViaje(int nodoOrigen){
        Vehiculo candidato=null;
        while(!this.colaDespacho.estaVacia()){
            candidato=(Vehiculo)this.colaDespacho.sacar();
            if((candidato).aceptaViaje())
                candidato.setState(EstadoVehiculo.OCUPADO);
        }
        return candidato;
    }
    public void muestraCovhes(){
        for(int i=0;i<this.vehiculos.size();i++)
            this.vehiculos.get(i).toString();
    }
}
