package simulador;

public class tes {

	public static void main(String[] args) {
	    servicios.LectorJSON miLector = new servicios.LectorJSON();
	    
	    // Le pasamos la ruta relativa hacia tu carpeta "data"
	    miLector.probarLecturaNodos("data/mapa_catedral_salta.json");
	}

}
/*cuando cambiemos el lector para comenzar a armar el grafo
ya no solo pasamos la ruta del archivo sino que tenemos que pasas
el contenedor vacio (el grafo) para que trabaje sobre él y comience a llenarlo con los datos extraidos
public void cargarMapa(AbsGrafoD miGrafo, String rutaArchivo) tenemos que modificar los parametros como se ve
En el primer bucle (Nodos), cambian el System.out.println por algo como:
miGrafo.insertarNodo(new NodoMapa(id, lat, lon));
En el segundo bucle (Calles), cambian el System.out.println por:
miGrafo.conectar(idOrigen, idDestino, tipoVia, nombreCalle);*/