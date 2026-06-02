package simulador;

public class tes {

	public static void main(String[] args) {
	    servicios.LectorJSON miLector = new servicios.LectorJSON();
	    
	    // Le pasamos la ruta relativa hacia tu carpeta "data"
	    miLector.probarLecturaNodos("data/mapa_catedral_salta.json");
	}

}
