package grafoDirigido;

import contenedores.ColaSLinkedList;
import contenedores.ListaDoubleLinkedL;
import contenedores.MatrizGrafo;
import recursos.*;
public abstract class AbsGrafo implements OperacionesG{
	protected MatrizGrafo matrizCosto;
	protected int ordenGrafo;
	protected static double infinito=10000;
	
	public AbsGrafo(int ordenGrafo) {
		this.ordenGrafo = ordenGrafo;
		this.matrizCosto = new MatrizGrafo(getOrden());
	}
	
	public int getOrden(){
		return this.ordenGrafo;
	}
	
	public abstract void cargarGrafo();
	public abstract void muestraGrafo();
	
	private void bpf(ListaDoubleLinkedL listaMarca, int v){
		boolean marcado;
		double currCost;
		
		listaMarca.reemplazar((true), v);
		System.out.println("vertice "+ v);
		for (int w=0;w<getOrden();w++){
			marcado=(boolean)listaMarca.devolver(w);
			currCost=(double)this.matrizCosto.devolver(v,w);
			if (currCost!=infinito && !marcado){
				bpf(listaMarca,w);
			}
		}
	}
	
	public void muestraBPF(){
		ListaDoubleLinkedL listaMarca;
		boolean marcado;
		
		listaMarca = new ListaDoubleLinkedL();
		for (int v=0;v<getOrden();v++){
			listaMarca.insertar(false, v);
		}
		
		for (int v=0;v<getOrden();v++){
			marcado=(boolean)listaMarca.devolver(v);
			if (!marcado){
				bpf(listaMarca,v);
			}
		}		
	}
	
	private void bea(ListaDoubleLinkedL listaMarca, int v){
		boolean marcado;
		double currCost;
		ColaSLinkedList cola;
		int w;
		
		listaMarca.reemplazar(true, v);
		System.out.println("vertice "+ v);
		cola = new ColaSLinkedList();
		cola.meter(v);
		
		while (!cola.estaVacia()){
			w=(int)cola.sacar();
			for (int z=0;z<getOrden();z++){
				marcado=(boolean)listaMarca.devolver(z);
				currCost=(double)this.matrizCosto.devolver(w,z);
				if (currCost!=infinito && !marcado){
					listaMarca.reemplazar(true, z);
					cola.meter(z);
					System.out.println("arista visitada " + w + " - " + z);
				}		
			}
		}
	}
	

	public void muestraBEA(){
		ListaDoubleLinkedL listaMarca;
		boolean marcado;
		
		listaMarca = new ListaDoubleLinkedL();
		for (int v=0;v<getOrden();v++){
			listaMarca.insertar(false, v);
		}
		
		for (int v=0;v<getOrden();v++){
			marcado=(boolean)listaMarca.devolver(v);
			if (!marcado){
				bea(listaMarca,v);
			}
		}		
	}
}
