package contenedores;
import recursos.*;

public abstract class Lista0DLinkedL implements OperacionesCL2{
	protected NodoDoble frenteL, finalL;
	protected int ultimo;
	
	public Lista0DLinkedL(){
		this.limpiar();
	}
	
	public void limpiar(){
		this.frenteL=this.finalL=null;
		this.ultimo=-1;		
	}
	
	public boolean estaVacia(){
		return (this.frenteL==null);
	}
	
	public int tamanio(){
		int cant=0;
		if (!estaVacia()){
			cant=this.ultimo+1;			
		}
		return cant;
	}
	
	
		
	public void eliminar(int posicion){
		if (estaVacia()){
			System.out.println("Error eliminar. Lista vacia...");
		}else{		
			if (posicion>=tamanio() || posicion<0){
				System.out.println("Error eliminar. Posicion inexistente ");
			}else{				
				if (posicion==0){ 
					if (this.frenteL==this.finalL){
						limpiar();						
					}else{
						this.frenteL=this.frenteL.getNextNodo();
						this.frenteL.setPrevNodo(null);
						this.ultimo--;
					}
				}else{
					if (posicion==tamanio()-1){ 
						this.finalL= this.finalL.getPrevNodo();
						this.finalL.setNextNodo(null);						
					}else{						
						NodoDoble prev, next;
						prev=this.frenteL;
						next=this.frenteL.getNextNodo();
						for (int counter=1; counter<posicion;counter++){
							prev=prev.getNextNodo(); next=next.getNextNodo();						
						}							
						
						next = next.getNextNodo();
						prev.setNextNodo(next); // actualizo referencias
						next.setPrevNodo(prev);				    	
					}
					this.ultimo--;
				}				
			}
		}
		
	}
		
	
	public Object devolver(int posicion){
		Object elemento=null;
		if (estaVacia()){
			System.out.println("Error devolver. Lista vacia...");
		} else {
			if (posicion>=tamanio() || posicion<0){
				System.out.println("Error devolver. La posicion no existe..");
			}else{
				NodoDoble temp;
				temp=this.frenteL;
				
				for (int counter=0; counter<posicion;counter++){						
					temp=temp.getNextNodo();		
				}				
				elemento=temp.getNodoInfo();
			}						
		}	
		return elemento;

	}	
	
	public abstract int buscar(Object elemento);

}
