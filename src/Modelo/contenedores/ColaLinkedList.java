package contenedores;
import recursos.Nodo;
import recursos.OperacionesCL1;
public abstract class ColaLinkedList implements OperacionesCL1{
	protected Nodo frenteC, finalC;
	
	public ColaLinkedList(){
		limpiar();
	}

	public boolean estaVacia(){
		return (this.frenteC==null);} 


	public abstract void meter(Object elemento);
	
	public Object sacar(){
		Object elemento = null;
		if (!estaVacia()){
			elemento=this.frenteC.getNodoInfo();
			this.frenteC=this.frenteC.getNextNodo();
			if (estaVacia()){
				this.finalC=null; 
			}
		}else{
			System.out.println("Error sacar. Cola vacia");
		}
		return elemento;
	}
	
	public void limpiar(){
		this.frenteC=this.finalC=null;
	}	
	
}
