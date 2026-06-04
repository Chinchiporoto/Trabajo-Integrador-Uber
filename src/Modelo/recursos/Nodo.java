package recursos;

public class Nodo {
	private Object nodoInfo;
	private Nodo nextNodo;
	
	public Nodo(Object nodoInfo){
		this(nodoInfo,null);
	} 
	
	public Nodo(Object nodoInfo, Nodo nextNodo){
		this.nodoInfo=nodoInfo;
		this.nextNodo=nextNodo; 
	}
	public void setNodoInfo(Object nodoInfo){
		this.nodoInfo=nodoInfo; 
	}
	
	public void setNextNodo(Nodo nextNodo){
		this.nextNodo=nextNodo; 
	}
	
	public Object getNodoInfo(){
		return this.nodoInfo; 
	}
	public Nodo getNextNodo(){
		return this.nextNodo; 
	}
	
	
}
