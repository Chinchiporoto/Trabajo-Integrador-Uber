package Modelo.simulador;
import java.util.ArrayList;

import Modelo.grafoDirigido.GrafoSalta;
import Modelo.recursos.NodoMapa;

/**
 * Representa un vehículo de transporte en el sistema de despacho.
 * 
 * <p>Cada vehículo mantiene información sobre su posición actual en la red de transporte,
 * su estado (disponible u ocupado), una ruta asignada y su tiempo estimado de llegada (ETA).</p>
 * 
 * <p>Características principales:</p>
 * <ul>
 *   <li>Posicionamiento continuo mediante interpolación (LERP) entre nodos</li>
 *   <li>Movimiento suave a través de rutas predefinidas</li>
 *   <li>Control de velocidad adaptativo basado en delta time</li>
 *   <li>Manejo de coordenadas decimales (latitud/longitud) para precisión visual</li>
 *   <li>Estados de disponibilidad (DISPONIBLE/OCUPADO)</li>
 * </ul>
 * 
 * @author Proyecto AYED
 * @version 1.0
 */
public class Vehiculo {
    /** Identificador único del vehículo */
    private int id;
    /** Nodo actual donde se encuentra el vehículo en el grafo */
    private int nodoActual;
    /** Tiempo estimado de llegada (ETA) al destino en segundos */
    private double eta;
    /** Lista de IDs de nodos que componen la ruta asignada */
    private ArrayList<Integer> rutaAsignada = new ArrayList<>();
    /** Estado actual del vehículo (DISPONIBLE u OCUPADO) */
    private EstadoVehiculo state;
    /** Coordenada de latitud decimal para posición continua entre nodos */
    private double latActualDecimal = 0.0;
    /** Coordenada de longitud decimal para posición continua entre nodos */
    private double lngActualDecimal = 0.0;
    /** Indica si las coordenadas decimales han sido inicializadas */
    private boolean coordenadasInicializadas = false;
    /** Timestamp del último cálculo de movimiento en milisegundos */
    private long ultimoTiempoMilis = 0;
    /**
     * Constructor que crea un nuevo vehículo en el sistema.
     * 
     * <p>Inicializa el vehículo con un identificador único y lo posiciona en un nodo específico
     * del grafo. El vehículo nace en estado DISPONIBLE con ETA de 0 segundos.</p>
     * 
     * <p>Las coordenadas decimales se inicializan con la posición real del nodo base,
     * permitiendo que el vehículo tenga una posición continua desde el inicio.</p>
     * 
     * @param iD identificador único del vehículo
     * @param nodo ID del nodo donde se posiciona inicialmente
     * @param grafo el grafo de la ciudad necesario para obtener coordenadas del nodo
     * 
     * @see EstadoVehiculo
     */
   public Vehiculo(int iD, int nodo, Modelo.grafoDirigido.GrafoSalta grafo){
        this.id = iD;
        this.nodoActual = nodo;
        this.state = EstadoVehiculo.DISPONIBLE;
        this.eta = 0.0;
        
        // Inicializamos las coordenadas decimales con la posición real de su nodo base
        Modelo.recursos.NodoMapa nodoBase = grafo.getNodo(nodo);
        if (nodoBase != null) {
            this.latActualDecimal = nodoBase.getLatitud();
            this.lngActualDecimal = nodoBase.getLongitud();
            this.coordenadasInicializadas = true; // Ya nacen con posición real
        }
    }
    /**
     * Retorna la ruta actual asignada al vehículo.
     * 
     * @return ArrayList de IDs de nodos que conforman la ruta
     */
    public ArrayList<Integer> getRutaAsignada() {
        return rutaAsignada;
    }
    /**
     * Determina si el vehículo aceptará un viaje solicitado.
     * 
     * <p>Usa una probabilidad del 90% de aceptación para simular el comportamiento
     * realista de vehículos que ocasionalmente rechazan viajes.</p>
     * 
     * @return true si acepta el viaje (90% probabilidad), false en caso contrario
     */
    public boolean aceptaViaje(){
        return Math.random()<0.9;
    }
    /**
     * Retorna el tiempo estimado de llegada (ETA) actual.
     * 
     * @return ETA en segundos
     */
    public double getEta() {
        return eta;
    }
    
    /**
     * Retorna el identificador único del vehículo.
     * 
     * @return ID del vehículo
     */
    public int getId() {
        return id;
    }
    
    /**
     * Retorna el nodo actual donde se encuentra el vehículo.
     * 
     * @return ID del nodo actual
     */
    public int getNodoActual() {
        return nodoActual;
    }
    
    /**
     * Retorna el estado actual del vehículo.
     * 
     * @return estado (DISPONIBLE o OCUPADO)
     * @see EstadoVehiculo
     */
    public EstadoVehiculo getState() {
        return state;
    }
    /**
     * Establece el nodo actual del vehículo.
     * 
     * @param nodoActual ID del nuevo nodo
     */
    public void setNodoActual(int nodoActual) {
        this.nodoActual = nodoActual;
    }
    
    /**
     * Establece el tiempo estimado de llegada (ETA).
     * 
     * @param eta el ETA en segundos
     */
    public void setEta(double eta) {
        this.eta = eta;
    }
    
    /**
     * Establece el estado del vehículo.
     * 
     * @param state el nuevo estado
     * @see EstadoVehiculo
     */
    public void setState(EstadoVehiculo state) {
        this.state = state;
    }
    
    /**
     * Establece la ruta completa asignada al vehículo.
     * 
     * @param rutaAsignada ArrayList de IDs de nodos que forman la nueva ruta
     */
    public void setRutaAsignada(ArrayList<Integer> rutaAsignada) {
        this.rutaAsignada = rutaAsignada;
    }
    /**
     * Retorna la latitud decimal actual del vehículo.
     * 
     * <p>Utiliza interpolación LERP entre nodos para proporcionar una posición continua
     * y suave durante el movimiento.</p>
     * 
     * @return latitud en grados decimales, o 0.0 si no está inicializado
     */
    public double getLatDecimal() { return coordenadasInicializadas ? latActualDecimal : 0.0; }
    
    /**
     * Retorna la longitud decimal actual del vehículo.
     * 
     * <p>Utiliza interpolación LERP entre nodos para proporcionar una posición continua
     * y suave durante el movimiento.</p>
     * 
     * @return longitud en grados decimales, o 0.0 si no está inicializado
     */
    public double getLngDecimal() { return coordenadasInicializadas ? lngActualDecimal : 0.0; }

    /**
     * Avanza el vehículo un paso hacia el siguiente nodo en su ruta.
     * 
     * <p>Este método realiza:</p>
     * <ul>
     *   <li>Cálculo del delta time desde el último movimiento</li>
     *   <li>Interpolación continua (LERP) entre el nodo actual y el siguiente</li>
     *   <li>Detección de llegada al siguiente nodo basada en distancia umbral</li>
     *   <li>Cambio automático de estado a DISPONIBLE cuando la ruta se completa</li>
     * </ul>
     * 
     * <p>La velocidad se adapta automáticamente y está limitada a 1.0 para evitar
     * saltos de nodo. El movimiento es suave gracias a la interpolación.</p>
     * 
     * @param grafo el grafo necesario para obtener información de los nodos
     * @return true si el vehículo llegó a un nodo o completó su ruta, false en caso contrario
     * 
     * @see #resetearRelojMecanico()
     */
    public boolean avanzarUnNodo(GrafoSalta grafo) {
        if (this.rutaAsignada != null && !this.rutaAsignada.isEmpty()) {
        NodoMapa siguienteNodo = grafo.getNodo(this.rutaAsignada.get(0));
        NodoMapa nodoBase = grafo.getNodo(this.nodoActual);
        
        if (siguienteNodo != null && nodoBase != null) {
            long tiempoActual = System.currentTimeMillis();
            if (!coordenadasInicializadas || this.ultimoTiempoMilis==0) {
                this.latActualDecimal = nodoBase.getLatitud();
                this.lngActualDecimal = nodoBase.getLongitud();
                this.coordenadasInicializadas = true;
                this.ultimoTiempoMilis = tiempoActual;
            }
            double deltaTime = (tiempoActual - this.ultimoTiempoMilis) / 1000.0;
            this.ultimoTiempoMilis = tiempoActual;
            if (deltaTime > 0.1) deltaTime = 0.03;
            double factorVelocidadEstable = 4.5 * deltaTime;
            if (factorVelocidadEstable > 1.0) factorVelocidadEstable = 1.0;
            // Interpolación continua (LERP)
            this.latActualDecimal += (siguienteNodo.getLatitud() - this.latActualDecimal) * factorVelocidadEstable; 
            this.lngActualDecimal += (siguienteNodo.getLongitud() - this.lngActualDecimal) * factorVelocidadEstable; 

            double distanciaUmbral = 0.00005;
            if (Math.abs(this.latActualDecimal - siguienteNodo.getLatitud()) < distanciaUmbral && 
                Math.abs(this.lngActualDecimal - siguienteNodo.getLongitud()) < distanciaUmbral) {
                
                this.nodoActual = this.rutaAsignada.remove(0); 
                
                if (this.rutaAsignada.isEmpty()) {
                    this.state = EstadoVehiculo.DISPONIBLE;
                    this.eta = 0.0;
                    // CORREGIDO: NO apagamos coordenadasInicializadas, el auto sigue existiendo en el espacio decimal
                    return true; 
                }
                return true; 
            }
        }
    }else{
        this.ultimoTiempoMilis = 0;
    }
    return false;
}
    /**
     * Reinicia el reloj mecánico del movimiento del vehículo.
     * 
     * <p>Se utiliza para sincronizar el delta time después de eventos importantes
     * o para prevenir saltos de tiempo excesivos.</p>
     */
    public void resetearRelojMecanico() {
        this.ultimoTiempoMilis = System.currentTimeMillis();
    }
/**
     * Retorna una representación en string del vehículo.
     * 
     * <p>Formato: "Movil [ID] [ETA: [valor] segs]"</p>
     * 
     * @return string descriptivo del vehículo
     */
@Override
public String toString(){
    return "Movil " + id + " [ETA: " + String.format("%.1f", eta) + " segs]";
}
}


