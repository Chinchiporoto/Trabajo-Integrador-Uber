package Modelo.Patrones;

import java.util.ArrayList;


import Modelo.grafoDirigido.GrafoSalta;

import Modelo.recursos.RutaAsignada;

public class FloydStrategy extends AbstractStrategy {
    @Override
    protected RutaAsignada calcular(GrafoSalta grafo, int idxOrigen, int idxDestino) {
        double costo = grafo.obtenerCostoFloyd(idxOrigen, idxDestino);
        ArrayList<Integer> camino = grafo.recuperarCaminoFloyd(idxOrigen, idxDestino);
        return new RutaAsignada(costo, camino);
    }
}
