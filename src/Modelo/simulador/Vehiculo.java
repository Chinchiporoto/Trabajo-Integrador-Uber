package simulador;
import java.util.ArrayList;
public class Vehiculo {
    private int id;
    private int nodoActual;
    private double eta;
    private ArrayList<Integer> rutaAsignada = new ArrayList<>();
    private EstadoVehiculo state;
    
    public Vehiculo(int iD, int nodo){
        this.id=iD;
        this.nodoActual=nodo;
        this.state=EstadoVehiculo.DISPONIBLE;
        this.eta=0.0;
    }
    public ArrayList<Integer> getRutaAsignada() {
        return rutaAsignada;
    }
    public boolean aceptaViaje(){
        return Math.random()<0.7;
    }
    public double getEta() {
        return eta;
    }
    public int getId() {
        return id;
    }
    public int getNodoActual() {
        return nodoActual;
    }
    public EstadoVehiculo getState() {
        return state;
    }
    public void setNodoActual(int nodoActual) {
        this.nodoActual = nodoActual;
    }
    public void setEta(double eta) {
        this.eta = eta;
    }
    public void setState(EstadoVehiculo state) {
        this.state = state;
    }
    public void setRutaAsignada(ArrayList<Integer> rutaAsignada) {
        this.rutaAsignada = rutaAsignada;
    }
@Override
public String toString(){
    return "Movil " + id + " [ETA: " + String.format("%.1f", eta) + " segs]";
    }
}


