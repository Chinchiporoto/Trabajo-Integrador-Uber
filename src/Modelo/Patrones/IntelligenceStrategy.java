package Modelo.Patrones;

import Modelo.grafoDirigido.AbsGrafo;
import Modelo.recursos.NodoMapa;
import Modelo.recursos.RutaAsignada;

public interface IntelligenceStrategy {
    public RutaAsignada calculaETA(AbsGrafo a, NodoMapa origin, NodoMapa destino);
}
