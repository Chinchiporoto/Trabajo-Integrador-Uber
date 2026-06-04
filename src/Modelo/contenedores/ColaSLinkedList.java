package contenedores;
import recursos.Nodo;
public class ColaSLinkedList extends ColaLinkedList{

	public void meter(Object elemento){
		if (!estaVacia()){
			this.finalC.setNextNodo(new Nodo(elemento));
			this.finalC=this.finalC.getNextNodo();
			// nuevo nodo es el ultimo.
		}else{
			this.frenteC=this.finalC= new Nodo(elemento);	
		}
	}
		
}
