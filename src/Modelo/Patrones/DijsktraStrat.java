package Modelo.Patrones;

import Modelo.recursos.RutaAsignada;

import Modelo.grafoDirigido.GrafoSalta;

public class DijsktraStrat extends AbstractStrategy {
    protected RutaAsignada calcular(GrafoSalta grafo, int idxOrigen, int idxDestino) {
        return grafo.dijkstraCompleto(idxOrigen, idxDestino);
    }
}
