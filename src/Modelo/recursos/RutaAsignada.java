package Modelo.recursos;

import java.util.ArrayList;

public class RutaAsignada {
    private double eta;
    private ArrayList<Integer> caminoNodos;
    public static final double ETA_INVALIDA = 10000.0;

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

    public boolean esInvalida() {
        return this.eta >= ETA_INVALIDA;
    }

    public static RutaAsignada invalida() {
        return new RutaAsignada(ETA_INVALIDA, new ArrayList<>());
    }
}