package contenedores;
import recursos.Connection;
import recursos.Nodo;

public class GraphPriorityQueue extends ColaPrioridad {
	
	
	public boolean esMenor(Object objA, Object objB){
		boolean response=false;
				
		if (((Connection)objA).getConnectionCost()<((Connection)objB).getConnectionCost()){
			response=true;
		}
		return response;
	}

	public boolean esMayor(Object objA, Object objB){
		boolean response=false;

		if (((Connection)objA).getConnectionCost()>((Connection)objB).getConnectionCost()){
			response=true;
		}	
		return response;
	}
	
	public boolean iguales(Object objA, Object objB){
		boolean response=false;
	
		if (((Connection)objA).getConnectionCost()==((Connection)objB).getConnectionCost()){
			response=true;
		}	
		return response;
	}
	
	
	void muestra(){
		Nodo temp; Connection conexion;		
		if (!estaVacia()){
			temp=this.frenteC;
			while (temp!=null){
				conexion=(Connection)temp.getNodoInfo();
				System.out.println("Conexion " + conexion.getVertexI() + " - " + conexion.getVertexJ() + ":" + conexion.getConnectionCost());
				temp=temp.getNextNodo();
			}			
		}else{
			System.out.println("Error muestra. Cola vacia");
		}		
	}	
}
