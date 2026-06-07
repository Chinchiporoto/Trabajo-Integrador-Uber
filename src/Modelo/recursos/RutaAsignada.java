package Modelo.recursos;


import java.util.ArrayList;

public class RutaAsignada {
    private double eta;
    private ArrayList<Integer> caminoNodos;

    public RutaAsignada(double eta, ArrayList<Integer> caminoNodos) {
        this.eta = eta;
        this.caminoNodos = caminoNodos;
    }

    public double getEta() {
        return eta;
    }

    public ArrayList<Integer> getCaminoNodos() {
        return caminoNodos;
    }
}