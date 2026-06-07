package Modelo.Patrones;

import java.util.ArrayList;

import Modelo.grafoDirigido.AbsGrafo;
import Modelo.grafoDirigido.AbsGrafoD;
import Modelo.grafoDirigido.GrafoSalta;
import Modelo.recursos.NodoMapa;
import Modelo.recursos.RutaAsignada;

public class FloydStrategy implements IntelligenceStrategy {
    @Override
    public RutaAsignada calculaETA(AbsGrafo a, NodoMapa origin, NodoMapa destino) {
        AbsGrafoD grafo = (AbsGrafoD) a;
        int idxOrigen = ((GrafoSalta) a).buscarIndice(origin.getId());
        int idxDestino = ((GrafoSalta) a).buscarIndice(destino.getId());
        if (idxOrigen == -1 || idxDestino == -1) return new RutaAsignada(10000.0, new ArrayList<>());
        double costo = grafo.obtenerCostoFloyd(idxOrigen, idxDestino);
        ArrayList<Integer> caminoReal = ((GrafoSalta) a).recuperarCaminoFloyd(idxOrigen, idxDestino);
        return new RutaAsignada(costo, caminoReal);
    }
}
