package Patrones;

import grafoDirigido.AbsGrafo;
import recursos.NodoMapa;

public interface IntelligenceStrategy {
    public double calculaETA(AbsGrafo a, NodoMapa origin, NodoMapa destino);
}
