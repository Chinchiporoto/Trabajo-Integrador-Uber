package simulador;

import contenedores.AbsGrafo;
import grafoDirigido.AbsGrafoD;
import recursos.NodoMapa;

public class FloydStrategy implements IntelligenceStrategy {
    public double calculaETA(AbsGrafo a, NodoMapa origin, NodoMapa destino) {
        AbsGrafoD grafo = (AbsGrafoD) a;
        int idxOrigen = ((grafoDirigido.GrafoSalta) a).buscarIndice(origin.getId());
        int idxDestino = ((grafoDirigido.GrafoSalta) a).buscarIndice(destino.getId());
        if (idxOrigen == -1 || idxDestino == -1) return 10000.0;
        return grafo.obtenerCostoFloyd(idxOrigen, idxDestino);
    }
}
