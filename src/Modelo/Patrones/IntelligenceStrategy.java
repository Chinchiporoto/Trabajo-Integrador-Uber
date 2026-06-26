package Modelo.Patrones;
import Modelo.grafoDirigido.GrafoSalta;

import Modelo.recursos.NodoMapa;
import Modelo.recursos.RutaAsignada;

public interface IntelligenceStrategy {
    RutaAsignada calculaETA(GrafoSalta grafo, NodoMapa origin, NodoMapa destino);
}
