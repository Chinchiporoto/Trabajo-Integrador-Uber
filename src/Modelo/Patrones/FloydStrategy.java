package Patrones;

import java.util.ArrayList;

import grafoDirigido.AbsGrafo;
import grafoDirigido.AbsGrafoD;
import recursos.NodoMapa;
import recursos.RutaAsignada;

public class FloydStrategy implements IntelligenceStrategy {
    @Override
    public RutaAsignada calculaETA(AbsGrafo a, NodoMapa origin, NodoMapa destino) {
        AbsGrafoD grafo = (AbsGrafoD) a;
        int idxOrigen = ((grafoDirigido.GrafoSalta) a).buscarIndice(origin.getId());
        int idxDestino = ((grafoDirigido.GrafoSalta) a).buscarIndice(destino.getId());
        if (idxOrigen == -1 || idxDestino == -1) return new RutaAsignada(10000.0, new ArrayList<>());
        return new RutaAsignada(grafo.obtenerCostoFloyd(idxOrigen, idxDestino), new ArrayList<>());
    }
}
