package Patrones;

import grafoDirigido.AbsGrafo;
import recursos.NodoMapa;
import recursos.RutaAsignada;

public interface IntelligenceStrategy {
    public RutaAsignada calculaETA(AbsGrafo a, NodoMapa origin, NodoMapa destino);
}
