package grafoDirigido;
import contenedores.*;
import recursos.*;

public abstract class AbsGrafoD extends AbsGrafo implements OperacionesGD{
	
	protected MatrizGrafo matrizCostoF,matrizCaminoF;
	protected ListaDoubleLinkedL listaDistancia, listaCamino, listaSolucion;

	public AbsGrafoD(int ordenGrafo){
		super(ordenGrafo);
	}
		
	public abstract void cargarGrafo();
	
	public void muestraDijkstra(int startVertex){
		double currCost; int w;
		
		Dijkstra(startVertex);
		
		for (int v=0; v<getOrden();v++){
			System.out.println("vertice " + v);
			if (v!=startVertex){
				currCost=(double)this.listaDistancia.devolver(v);
				System.out.println("costo desde " + startVertex + " a " + v + "->" + currCost);
			
				System.out.println("mostrando un camino desde "+ v + " a " + startVertex);
				
				w=(int)this.listaCamino.devolver(v);
				
				do{
					System.out.println("camino " + w);
					w=(int)this.listaCamino.devolver(w);
				}while(w!=-1);//recordemos que al inicializar cambiamos todos los -1 salvo el startVertex
			}			
		}
		
	}
	
	private void Dijkstra(int startVertex){
		double minCost, currCost, arcCost; int minVertex, vertex;
		
		this.listaDistancia = new ListaDoubleLinkedL();
		this.listaCamino = new ListaDoubleLinkedL();
		this.listaSolucion = new ListaDoubleLinkedL();
		
		for (int i=0; i<getOrden();i++){			
			this.listaSolucion.insertar(-1, i);
			this.listaCamino.insertar(-1, i);
			this.listaDistancia.insertar(infinito, i);
		}
		this.listaSolucion.reemplazar(startVertex,startVertex); // el primer vertice del camino
		
		for (int i=0; i<getOrden();i++){
			if (i!=startVertex){
				this.listaDistancia.reemplazar(this.matrizCosto.devolver(startVertex, i), i);
				this.listaCamino.reemplazar(startVertex, i);					
			}
		}
				
		
		for (int i=1; i<getOrden();i++){
			minCost=infinito;
			minVertex=-1;
			
			for (int w=0; w<getOrden();w++){
				if (w!=startVertex){
					currCost=(double) this.listaDistancia.devolver(w);// 
					vertex=(int) this.listaSolucion.devolver(w);
					if (currCost<minCost && vertex==-1){
						minCost=currCost; minVertex=w;
					}
				}
			}
			
			if(minVertex!=-1){
				System.out.println("it " + i + " minVertex " + minVertex + " minCost " + minCost);
				this.listaSolucion.reemplazar(minVertex, minVertex);
				this.listaDistancia.reemplazar(minCost, minVertex);
					
				
				for (int v=0;v<getOrden();v++){
					vertex=(int)this.listaSolucion.devolver(v);
					if (vertex==-1){
						arcCost=(double)this.matrizCosto.devolver(minVertex, v);
						currCost=(double)this.listaDistancia.devolver(v);
						if (minCost+arcCost<currCost){
							this.listaDistancia.reemplazar(minCost+arcCost, v);
							this.listaCamino.reemplazar(minVertex, v);
												
						}					
					}
				}
			}
		}		
	}


	
	public void muestraGrafo(){
		double currCost;
		for (int i=0; i<getOrden();i++){
			for (int j=0; j<getOrden();j++){
				if (i!=j){
					currCost=(double)this.matrizCosto.devolver(i, j);
					if (currCost!=infinito){
						System.out.println("costo " + i + " a " + j + "->" + currCost);
					}				
				}
			}			
		}		
	}
	
	
	public void muestraFloyd(){
		this.matrizCaminoF=new MatrizGrafo(this.ordenGrafo);
		this.matrizCostoF=new MatrizGrafo(this.ordenGrafo);
		double costoF;
		for(int i=0;i<ordenGrafo;i++){
			matrizCostoF.actualizar((double)0, i, i);}
		
		for(int i=0;i<ordenGrafo;i++){
			for(int j=0;j<ordenGrafo;j++){
				if(i!=j){
					costoF=(double)matrizCosto.devolver(i, j);
					matrizCostoF.actualizar(costoF, i, j);
				}
			}
		}
		
		
		
		Object costo;
		for(int k=0;k<ordenGrafo;k++){
			for(int i=0;i<ordenGrafo;i++){
				for(int j=0;j<ordenGrafo;j++){
					if(((Double)matrizCostoF.devolver(i, k)).doubleValue()+((Double)matrizCostoF.devolver(k, j)).doubleValue()<((Double)matrizCostoF.devolver(i, j)).doubleValue()){
						costo=(((Double)matrizCostoF.devolver(i, k)).doubleValue()+((Double)matrizCostoF.devolver(k, j)).doubleValue());
						matrizCostoF.actualizar(costo, i, j);
						matrizCaminoF.actualizar((k), i, j);//para obtener el camino de Floyd.
					}
				}
			}
		}
		System.out.println("Floyd: ");
		for(int i=0;i<ordenGrafo;i++){
			for(int j=0;j<ordenGrafo;j++){
				if(i!=j){
					costoF=(double)matrizCostoF.devolver(i, j);
					if(costoF!=infinito){System.out.println("Costo m�nimo de "+i+" hasta "+j+": "+costoF);}
				}
				
				
			}
		}
		
	}
	

	
	public void muestraCaminoFloyd(int origen, int destino){
		double hayCamino = ((Double)this.matrizCostoF.devolver(origen, destino)).doubleValue();
		if(hayCamino!=infinito) {
			System.out.print("Camino entre "+origen+" y "+destino+": ");
			System.out.print(origen);
			buscarCaminoFloyd(origen,destino);
			System.out.print(" "+destino);
			System.out.println();
		}else {
			System.out.println("NO hay Camino entre " + origen + " y " + destino);
		}	
	}
	
	private void buscarCaminoFloyd(int i, int j){
		Object valor=matrizCaminoF.devolver(i, j);
		if(valor!=null){
			int k=((Integer)valor).intValue();
			buscarCaminoFloyd(i,k);
			System.out.print(" "+k);
			buscarCaminoFloyd(k,j);
		}else{
			System.out.print(" |");
		}
	}
	public double obtenerCostoDijkstra(int origen, int destino) {
		Dijkstra(origen); 
		return (double) this.listaDistancia.devolver(destino);
	}
	public double obtenerCostoFloyd(int origen, int destino) {

		if (this.matrizCostoF == null) {
			this.muestraFloyd(); 
		}
		return (double) this.matrizCostoF.devolver(origen, destino);
	}
}
