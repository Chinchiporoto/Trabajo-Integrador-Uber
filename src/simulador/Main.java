package simulador;

import java.util.ArrayList;
import grafoDirigido.GrafoSalta;

public class Main { 
    public static void main(String[] args) {
            servicio.LectorJSON miLector = new servicio.LectorJSON();
	    
	    // Le pasamos la ruta relativa hacia tu carpeta "data"
	    miLector.probarLecturaNodos("data/mapa_catedral_salta.json");
	}
}
