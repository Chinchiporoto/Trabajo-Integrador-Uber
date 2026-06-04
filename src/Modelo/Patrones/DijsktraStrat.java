package Patrones;

import java.util.ArrayList;

import recursos.RutaAsignada;
import grafoDirigido.AbsGrafo;
import grafoDirigido.AbsGrafoD;
import recursos.NodoMapa;

public class DijsktraStrat implements IntelligenceStrategy {
    @Override
    public RutaAsignada calculaETA(AbsGrafo a, NodoMapa origin, NodoMapa destino) {
        AbsGrafoD grafo = (AbsGrafoD) a;
        int idxOrigen = ((grafoDirigido.GrafoSalta) a).buscarIndice(origin.getId());
        int idxDestino = ((grafoDirigido.GrafoSalta) a).buscarIndice(destino.getId());
        if (idxOrigen == -1 || idxDestino == -1) return new RutaAsignada(10000.0, new ArrayList<>());
        double costo = grafo.obtenerCostoDijkstra(idxOrigen, idxDestino);
        ArrayList<Integer> caminoReal = ((grafoDirigido.GrafoSalta) a).recuperarCaminoDijkstra(idxOrigen, idxDestino);
        return new RutaAsignada(costo, caminoReal);
    }
}

