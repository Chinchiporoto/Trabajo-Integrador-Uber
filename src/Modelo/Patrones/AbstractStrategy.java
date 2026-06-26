package Modelo.Patrones;

import Modelo.grafoDirigido.GrafoSalta;
import Modelo.recursos.NodoMapa;
import Modelo.recursos.RutaAsignada;

public abstract class AbstractStrategy implements IntelligenceStrategy {

    @Override
    public RutaAsignada calculaETA(GrafoSalta grafo, NodoMapa origin, NodoMapa destino) {
        int idxOrigen = grafo.buscarIndice(origin.getId());
        int idxDestino = grafo.buscarIndice(destino.getId());
        if (idxOrigen == -1 || idxDestino == -1)
            return RutaAsignada.invalida();
        return calcular(grafo, idxOrigen, idxDestino);
    }

    // Cada estrategia solo implementa su algoritmo
    protected abstract RutaAsignada calcular(GrafoSalta grafo, int idxOrigen, int idxDestino);

}