package Modelo.simulador;

import java.util.ArrayList;

import Modelo.recursos.RutaAsignada;
import Modelo.servicio.LectorJSON;
import Modelo.Patrones.DijsktraStrat;
import Modelo.Patrones.FloydStrategy;
import Modelo.Patrones.IntelligenceStrategy;
import Modelo.contenedores.VehiculoPriority;
import Modelo.recursos.NodoMapa;
import Modelo.grafoDirigido.AbsGrafo;
import Modelo.grafoDirigido.GrafoSalta;

/**
 * Gestor central del sistema de despacho de vehículos.
 * 
 * <p>Coordina la asignación inteligente de viajes a vehículos disponibles, gestiona
 * el movimiento de la flota y registra todos los eventos del sistema. Implementa
 * estrategias de enrutamiento adaptativas (Dijkstra para distancias cortas, Floyd para largas)
 * y un sistema de patrullaje para vehículos disponibles.</p>
 * 
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Registrar vehículos disponibles en una cola de prioridad (por distancia)</li>
 *   <li>Asignar viajes óptimos a vehículos basado en estrategia inteligente</li>
 *   <li>Validar rutas antes de asignarlas (evitar puntos muertos y rutas inválidas)</li>
 *   <li>Mover la flota en tiempo real con interpolación suave</li>
 *   <li>Patrullar vehículos disponibles por el mapa de forma aleatoria</li>
 *   <li>Registrar y distribuir logs de eventos del sistema</li>
 * </ul>
 * 
 * @author Proyecto AYED
 * @version 1.0
 */
public class Despachador {
	/** Lista de todos los vehículos en el sistema */
    protected ArrayList<Vehiculo> vehiculos;
    /** Cola de prioridad que ordena vehículos disponibles por distancia al pasajero */
    protected VehiculoPriority colaDespacho;
    /** Referencia al grafo de la ciudad (Salta) */
    private AbsGrafo map;
    /** Estrategia de Dijkstra para distancias cortas (< 1500m) */
    private IntelligenceStrategy metodoDji;
    /** Estrategia de Floyd para distancias largas (>= 1500m) */
    private IntelligenceStrategy metodoFlo;
    /** Nodo del pasajero actual/activo */
    private NodoMapa pasajeroAct=null;
    /** Registro temporal de eventos para la interfaz gráfica */
    private ArrayList<String> logsTemporales = new ArrayList<>();
    
    /**
     * Constructor que inicializa el despachador con una flota de vehículos.
     * 
     * <p>Crea las instancias de estrategias de enrutamiento (Dijkstra y Floyd)
     * y la cola de prioridad para gestionar vehículos disponibles.</p>
     * 
     * @param a ArrayList de vehículos que forman la flota
     * @param mapa el grafo que representa la ciudad (Salta)
     */
    public Despachador(ArrayList<Vehiculo>a, AbsGrafo mapa){
        this.colaDespacho=new VehiculoPriority();
        this.vehiculos=a;
        this.map=mapa;
        this.metodoDji= new DijsktraStrat();
        this.metodoFlo= new FloydStrategy();
    }
    /**
     * Retorna el nodo del pasajero actualmente activo.
     * 
     * @return NodoMapa del pasajero actual, o null si no hay pasajero activo
     */
    public NodoMapa getPasajeroAct() {
        return pasajeroAct;
    }
    
    /**
     * Retorna la cola de despacho con vehículos disponibles ordenados por distancia.
     * 
     * @return VehiculoPriority con vehículos disponibles
     */
    public VehiculoPriority getColaDespacho() {
        return this.colaDespacho;
    }
    
    /**
     * Establece el nodo del pasajero actualmente activo.
     * 
     * @param pasajero NodoMapa donde se encuentra el pasajero
     */
    public void setPasajeroAct(NodoMapa pasajero) {
        this.pasajeroAct = pasajero;
    }
    /**
     * Agrega un vehículo a la flota.
     * 
     * @param a el vehículo a agregar
     */
    public void cargaAutos(Vehiculo a){
        vehiculos.add(a);
    }
    
    /**
     * Limpia la cola de despacho.
     */
    public void limpiar(){
        this.colaDespacho.limpiar();
    }
    /**
     * Registra todos los vehículos disponibles en la cola de despacho.
     * 
     * <p>Para cada vehículo en estado DISPONIBLE, calcula la distancia Haversine
     * al nodo del pasajero y lo agrega a la cola ordenado por distancia (más cercano primero).</p>
     * 
     * @param nodePasajero ID del nodo donde se encuentra el pasajero
     */
    public void registrarDisponibles(int nodePasajero){ 
        limpiar();
        GrafoSalta gs = (GrafoSalta) this.map;
        NodoMapa nodoPasajero = gs.getNodo(nodePasajero);
        
        for(int i = 0; i < this.vehiculos.size(); i++) {
            Vehiculo v = vehiculos.get(i);
            if(v.getState() == EstadoVehiculo.DISPONIBLE){ 
                NodoMapa iAuto = gs.getNodo(v.getNodoActual());
                double costo = iAuto.distanciaHaversine(nodoPasajero);
                v.setEta(costo); 
                
                this.colaDespacho.meter(v);
            }
        }
    }

    /**
     * Asigna un viaje a un vehículo disponible.
     * 
     * <p>Este método:</p>
     * <ul>
     *   <li>Extrae vehículos de la cola de prioridad (ordenados por distancia)</li>
     *   <li>Verifica que cada vehículo acepte el viaje (90% probabilidad)</li>
     *   <li>Selecciona estrategia de enrutamiento basada en distancia (Dijkstra/Floyd)</li>
     *   <li>Valida que la ruta sea legal (sin puntos muertos, distancias razonables)</li>
     *   <li>Asigna la ruta al vehículo y lo marca como OCUPADO</li>
     *   <li>Continúa probando otros vehículos si la ruta es inválida</li>
     * </ul>
     * 
     * <p>Preventivamente congela la ruta del vehículo para evitar teletransportaciones
     * durante el cálculo de ruta en hilo secundario.</p>
     * 
     * @param nodoOrigen ID del nodo donde se origina la solicitud (pasajero)
     * @return el Vehiculo asignado al viaje, o null si no hay vehículos disponibles/válidos
     * 
     * @see IntelligenceStrategy
     * @see EstadoVehiculo
     */
    public Vehiculo asignaViaje(int nodoOrigen){
        Vehiculo candidato = null;
        GrafoSalta gs = (GrafoSalta) this.map;
        NodoMapa nodoPasajero = gs.getNodo(nodoOrigen);
    
    while(!this.colaDespacho.estaVacia()){
        candidato = (Vehiculo) this.colaDespacho.sacar();
        if(candidato.aceptaViaje()){ 
            
            // --- BLOQUEO ANTI-TELETRANSPORTACIÓN ---
            // Le congelamos el buffer de patrullaje INMEDIATAMENTE en el hilo secundario
            // para que deje de deambular y su posición decimal no se desplace mientras se calcula Dijkstra.
            int nodoPartidaReal = candidato.getNodoActual();
            if (candidato.getRutaAsignada() != null && !candidato.getRutaAsignada().isEmpty()) {
                nodoPartidaReal = candidato.getRutaAsignada().get(0);
            }
            
            NodoMapa nodoAuto = gs.getNodo(nodoPartidaReal);
            double distanciaRecta = candidato.getEta();
            Modelo.Patrones.IntelligenceStrategy Strat = (distanciaRecta < 1500) ? 
                this.metodoDji : this.metodoFlo;
            
            RutaAsignada ruta = Strat.calculaETA(this.map, nodoAuto, nodoPasajero);
            boolean esValida = true;
            if (ruta.getEta() >= 9999.0) {
                esValida = false;
            } else {
                for (int i = 0; i < ruta.getCaminoNodos().size() - 1; i++) {
                    NodoMapa n1 = gs.getNodo(ruta.getCaminoNodos().get(i));
                    NodoMapa n2 = gs.getNodo(ruta.getCaminoNodos().get(i+1));
                    if (n1.distanciaHaversine(n2) > 500.0) {
                        esValida = false;
                        break;
                    }
                }
            }
            if (!esValida) {
                this.registrarLog(" ↳ Móvil " + candidato.getId() + " ruta ilegal/aislada. Descartando...");
                continue; 
            }
            
            
            candidato.getRutaAsignada().clear();
           
            candidato.setNodoActual(nodoPartidaReal);
            
            candidato.getRutaAsignada().addAll(ruta.getCaminoNodos());
            candidato.setEta(ruta.getEta());
            candidato.resetearRelojMecanico(); 
            candidato.setState(EstadoVehiculo.OCUPADO);
            return candidato;
        }
    }
    return null;
} 
    /**
     * Muestra en consola la información de todos los vehículos.
     */
    public void muestraCoches(){
        for(int i=0;i<this.vehiculos.size();i++)
            System.out.println(this.vehiculos.get(i).toString());
    }
    
    /**
     * Crea un viaje aleatorio generando un pasajero en un nodo válido.
     * 
     * <p>Este método:</p>
     * <ul>
     *   <li>Genera un nodo pasajero aleatorio dentro de los límites del mapa</li>
     *   <li>Valida que el nodo no sea un punto muerto</li>
     *   <li>Registra el pasajero como activo</li>
     *   <li>Invoca automáticamente {@link #registrarDisponibles} y {@link #asignaViaje}</li>
     *   <li>Registra los eventos en el log del sistema</li>
     * </ul>
     * 
     * @param flota ArrayList de todos los vehículos disponibles
     * @param grafo el grafo de la ciudad (Salta)
     * 
     * @see #registrarDisponibles(int)
     * @see #asignaViaje(int)
     */
    public void crearViajeAleatorio(ArrayList<Vehiculo> flota, GrafoSalta grafo) {
        int maxNodos = grafo.getOrden();
        int nodoPasajeroAleatorio;
        NodoMapa pasajeroCandidato;
        double margen = 0.002;
        boolean nodoValido= false;
        

    do {
        nodoPasajeroAleatorio = (int) (Math.random() * maxNodos);
        pasajeroCandidato = grafo.getNodo(nodoPasajeroAleatorio);

        if ((pasajeroCandidato != null) && grafo.esPuntoMuerto(nodoPasajeroAleatorio))
            nodoValido=true;
    } while (pasajeroCandidato == null || !nodoValido||
             pasajeroCandidato.getLatitud() < (LectorJSON.LAT_MIN + margen) || 
             pasajeroCandidato.getLatitud() > (LectorJSON.LAT_MAX - margen) || 
             pasajeroCandidato.getLongitud() < (LectorJSON.LNG_MIN + margen) || 
             pasajeroCandidato.getLongitud() > (LectorJSON.LNG_MAX - margen));

    this.pasajeroAct = pasajeroCandidato;
    
    this.registrarLog("[PASAJERO] Solicitud generada en nodo: " + nodoPasajeroAleatorio);
    
    this.registrarDisponibles(nodoPasajeroAleatorio);
    Vehiculo asignar = this.asignaViaje(nodoPasajeroAleatorio);
    
    if(asignar != null)
        this.registrarLog("[DESPACHO] Viaje asignado al Móvil " + asignar.getId());
    else
        this.registrarLog("[ALERTA] No se pudo asignar ningún vehículo.");
}

    /**
     * Registra un mensaje de evento en el sistema.
     * 
     * <p>Imprime el mensaje en la consola y lo almacena temporalmente para 
     * ser procesado por la interfaz gráfica.</p>
     * 
     * @param msj el mensaje a registrar
     */
    public void registrarLog(String msj) {
        System.out.println(msj); // Lo seguimos imprimiendo en consola por las dudas
        this.logsTemporales.add(msj); // Lo guardamos para la interfaz
    }
    
    /**
     * Obtiene todos los logs temporales acumulados y limpia el buffer.
     * 
     * <p>Se usa en el hilo de UI para recuperar eventos registrados desde
     * el último update y mostrarlos en la interfaz gráfica.</p>
     * 
     * @return ArrayList con copias de los logs acumulados
     */
    public ArrayList<String> obtenerYLimpiarLogs() {
        ArrayList<String> copia = new ArrayList<>(this.logsTemporales);
        this.logsTemporales.clear();
        return copia;
    }
    /**
     * Actualiza el movimiento de toda la flota.
     * 
     * <p>Para cada vehículo:</p>
     * <ul>
     *   <li><b>OCUPADO:</b> Avanza hacia el siguiente nodo de su ruta asignada.
     *       Cuando llega a destino (ruta vacía), cambia a DISPONIBLE.</li>
     *   <li><b>DISPONIBLE:</b> Patrulla de forma aleatoria generando una ruta de 3 nodos.
     *       Si queda atrapado en un punto muerto, es rescatado y reubicado aleatoriamente.
     *       Avanza por su ruta de patrullaje.</li>
     * </ul>
     * 
     * <p>Este método es invocado continuamente (cada ~30ms) para simular el movimiento
     * en tiempo real con interpolación suave (LERP) entre nodos.</p>
     * 
     * @see Vehiculo#avanzarUnNodo(GrafoSalta)
     * @see GrafoSalta#obtenerVecinosValidos(int)
     * @see GrafoSalta#esPuntoMuerto(int)
     */
    public void moverFlota() {
    GrafoSalta gs = (GrafoSalta) this.map;
    
    for (Vehiculo v : this.vehiculos) {
        if (v.getState() == EstadoVehiculo.OCUPADO) {
            boolean llegoADestino = v.avanzarUnNodo(gs);
            if (llegoADestino && v.getRutaAsignada().isEmpty())
                this.registrarLog("[TRACKING] Móvil " + v.getId() + " llegó a destino y está DISPONIBLE.");
        }
        else if (v.getState() == EstadoVehiculo.DISPONIBLE) { 
        
        boolean atrapadoPuntoMuerto= false;
        
            if (v.getRutaAsignada() == null) 
                v.setRutaAsignada(new ArrayList<>());
            
            
            while (v.getRutaAsignada().size() < 3) {
                int nodoPunta = v.getRutaAsignada().isEmpty() ? 
                 v.getNodoActual() : v.getRutaAsignada().get(v.getRutaAsignada().size() - 1); 
            
                 ArrayList<Integer> vecinos = gs.obtenerVecinosValidos(nodoPunta);
                
                
                if (!vecinos.isEmpty()) {
                    int esquinaAzar = vecinos.get((int) (Math.random() * vecinos.size()));
                    v.getRutaAsignada().add(esquinaAzar); 
                }
                else {
                    atrapadoPuntoMuerto = true;
                    break; 
                }
            }
            if (atrapadoPuntoMuerto && v.getRutaAsignada().isEmpty()) {
                int nodoRescate = (int) (Math.random() * gs.getOrden());
                NodoMapa nRescate = gs.getNodo(nodoRescate);
            if (nRescate != null) {
                    v.setNodoActual(nodoRescate);
                    v.getRutaAsignada().clear();
                    v.resetearRelojMecanico(); 
                    System.out.println("[SISTEMA] Móvil " + v.getId() + " rescatado de punto muerto. Reubicado en nodo " + nodoRescate);
                }
            continue;
            }
    
            if (!v.getRutaAsignada().isEmpty()) {
                int nodoObjetivoInmediato = v.getRutaAsignada().get(0);
                boolean cruzoEsquina = v.avanzarUnNodo(gs);  
                if (cruzoEsquina) 
                   v.setNodoActual(nodoObjetivoInmediato);
            }
        }
}

}
}
