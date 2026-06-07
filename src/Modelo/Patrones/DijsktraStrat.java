package Modelo.Patrones;

import java.util.ArrayList;

import Modelo.recursos.RutaAsignada;
import Modelo.grafoDirigido.AbsGrafo;
import Modelo.grafoDirigido.AbsGrafoD;
import Modelo.grafoDirigido.GrafoSalta;
import Modelo.recursos.NodoMapa;

public class DijsktraStrat implements IntelligenceStrategy {
    @Override
    public RutaAsignada calculaETA(AbsGrafo a, NodoMapa origin, NodoMapa destino) {
        AbsGrafoD grafo = (AbsGrafoD) a;
        int idxOrigen = ((GrafoSalta) a).buscarIndice(origin.getId());
        int idxDestino = ((GrafoSalta) a).buscarIndice(destino.getId());
        if (idxOrigen == -1 || idxDestino == -1) return new RutaAsignada(10000.0, new ArrayList<>());
        double costo = grafo.obtenerCostoDijkstra(idxOrigen, idxDestino);
        ArrayList<Integer> caminoReal = ((GrafoSalta) a).recuperarCaminoDijkstra(idxOrigen, idxDestino);
        return new RutaAsignada(costo, caminoReal);
    }
}

