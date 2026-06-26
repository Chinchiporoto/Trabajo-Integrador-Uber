package Modelo.simulador;

import java.util.ArrayList;
import Modelo.grafoDirigido.GrafoSalta;
import Modelo.recursos.NodoMapa;

/**
 * Representa un vehículo de transporte en el sistema de despacho.
 * 
 * <p>
 * Cada vehículo mantiene información sobre su posición actual en la red de
 * transporte,
 * su estado (disponible u ocupado), una ruta asignada y su tiempo estimado de
 * llegada (ETA).
 * </p>
 * 
 * <p>
 * Características principales:
 * </p>
 * <ul>
 * <li>Posicionamiento continuo mediante interpolación (LERP) entre nodos</li>
 * <li>Movimiento suave a través de rutas predefinidas</li>
 * <li>Control de velocidad adaptativo basado en delta time</li>
 * <li>Manejo de coordenadas decimales (latitud/longitud) para precisión
 * visual</li>
 * <li>Estados de disponibilidad (DISPONIBLE/OCUPADO)</li>
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
    private int nodoAnterior = -1;
    private int nodoDestinoFinal = -1;
    private ArrayList<Integer> rutaFase2 = null;
    private double etaFase2 = 0.0;

    /**
     * Constructor que crea un nuevo vehículo en el sistema.
     * 
     * <p>
     * Inicializa el vehículo con un identificador único y lo posiciona en un nodo
     * específico
     * del grafo. El vehículo nace en estado DISPONIBLE con ETA de 0 segundos.
     * </p>
     * 
     * <p>
     * Las coordenadas decimales se inicializan con la posición real del nodo base,
     * permitiendo que el vehículo tenga una posición continua desde el inicio.
     * </p>
     * 
     * @param iD    identificador único del vehículo
     * @param nodo  ID del nodo donde se posiciona inicialmente
     * @param grafo el grafo de la ciudad necesario para obtener coordenadas del
     *              nodo
     * 
     * @see EstadoVehiculo
     */
    public Vehiculo(int iD, int nodo, Modelo.grafoDirigido.GrafoSalta grafo) {
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

    public void setRutaFase2(ArrayList<Integer> ruta) {
        this.rutaFase2 = ruta;
    }

    public ArrayList<Integer> getRutaFase2() {
        return this.rutaFase2;
    }

    public void setEtaFase2(double e) {
        this.etaFase2 = e;
    }

    public double getEtaFase2() {
        return this.etaFase2;
    }

    public int getNodoDestinoFinal() {
        return this.nodoDestinoFinal;
    }

    public void setNodoDestinoFinal(int nodoDestino) {
        this.nodoDestinoFinal = nodoDestino;
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
     * <p>
     * Usa una probabilidad del 90% de aceptación para simular el comportamiento
     * realista de vehículos que ocasionalmente rechazan viajes.
     * </p>
     * 
     * @return true si acepta el viaje (90% probabilidad), false en caso contrario
     */
    public boolean aceptaViaje() {
        return Math.random() < 0.9;
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
     * <p>
     * Utiliza interpolación LERP entre nodos para proporcionar una posición
     * continua
     * y suave durante el movimiento.
     * </p>
     * 
     * @return latitud en grados decimales, o 0.0 si no está inicializado
     */
    public double getLatDecimal() {
        return coordenadasInicializadas ? latActualDecimal : 0.0;
    }

    /**
     * Retorna la longitud decimal actual del vehículo.
     * 
     * <p>
     * Utiliza interpolación LERP entre nodos para proporcionar una posición
     * continua
     * y suave durante el movimiento.
     * </p>
     * 
     * @return longitud en grados decimales, o 0.0 si no está inicializado
     */
    public double getLngDecimal() {
        return coordenadasInicializadas ? lngActualDecimal : 0.0;
    }

    /**
     * Avanza el vehículo un paso hacia el siguiente nodo en su ruta.
     * 
     * <p>
     * Este método realiza:
     * </p>
     * <ul>
     * <li>Cálculo del delta time desde el último movimiento</li>
     * <li>Interpolación continua (LERP) entre el nodo actual y el siguiente</li>
     * <li>Detección de llegada al siguiente nodo basada en distancia umbral</li>
     * <li>Cambio automático de estado a DISPONIBLE cuando la ruta se completa</li>
     * </ul>
     * 
     * <p>
     * La velocidad se adapta automáticamente y está limitada a 1.0 para evitar
     * saltos de nodo. El movimiento es suave gracias a la interpolación.
     * </p>
     * 
     * @param grafo el grafo necesario para obtener información de los nodos
     * @return true si el vehículo llegó a un nodo o completó su ruta, false en caso
     *         contrario
     * 
     * @see #resetearRelojMecanico()
     */
    public boolean avanzarUnNodo(GrafoSalta grafo) {
        if (this.rutaAsignada != null && !this.rutaAsignada.isEmpty()) {
            NodoMapa siguienteNodo = grafo.getNodo(this.rutaAsignada.get(0));
            NodoMapa nodoBase = grafo.getNodo(this.nodoActual);

            if (siguienteNodo != null && nodoBase != null) {
                long tiempoActual = System.currentTimeMillis();
                if (!coordenadasInicializadas || this.ultimoTiempoMilis == 0) {
                    this.latActualDecimal = nodoBase.getLatitud();
                    this.lngActualDecimal = nodoBase.getLongitud();
                    this.coordenadasInicializadas = true;
                    this.ultimoTiempoMilis = tiempoActual;
                }

                double deltaTime = (tiempoActual - this.ultimoTiempoMilis) / 1000.0;
                this.ultimoTiempoMilis = tiempoActual;

                // Escudo anti-lag: Si la CPU se cuelga un instante, evitamos que el auto se
                // teletransporte
                if (deltaTime > 0.1)
                    deltaTime = 0.016;

                // --- NUEVA MATEMÁTICA: MOVIMIENTO VECTORIAL CONSTANTE ---
                // 1. Calculamos el vector de dirección y la distancia geométrica exacta
                double dLat = siguienteNodo.getLatitud() - this.latActualDecimal;
                double dLng = siguienteNodo.getLongitud() - this.lngActualDecimal;
                double distanciaRestante = Math.sqrt(dLat * dLat + dLng * dLng);

                // 2. Definimos la velocidad según el estado del auto (DISPONIBLE va paseando,
                // OCUPADO va rápido)
                double velocidad = (this.state == EstadoVehiculo.DISPONIBLE) ? 0.0006 : 0.0013;
                double paso = velocidad * deltaTime;

                // 3. Verificamos si el paso que va a dar alcanza para llegar a la esquina
                if (distanciaRestante <= paso || distanciaRestante < 0.00001) {
                    // Llegó exacto a la esquina: lo "encajamos" matemáticamente en el vértice
                    this.latActualDecimal = siguienteNodo.getLatitud();
                    this.lngActualDecimal = siguienteNodo.getLongitud();
                    this.nodoAnterior = this.nodoActual;
                    this.nodoActual = this.rutaAsignada.remove(0);

                    if (this.rutaAsignada.isEmpty()) {
                        this.eta = 0.0;
                        return true; // Termino toda la ruta
                    }
                    return true; // Llego a un nodo intermedio
                } else {
                    // Aún le falta: Normalizamos el vector y lo multiplicamos por nuestro paso
                    // constante
                    this.latActualDecimal += (dLat / distanciaRestante) * paso;
                    this.lngActualDecimal += (dLng / distanciaRestante) * paso;
                }
            }
        } else {
            this.ultimoTiempoMilis = 0;
        }
        return false;
    }

    /**
     * Reinicia el reloj mecánico del movimiento del vehículo.
     * 
     * <p>
     * Se utiliza para sincronizar el delta time después de eventos importantes
     * o para prevenir saltos de tiempo excesivos.
     * </p>
     */
    public void resetearRelojMecanico() {
        this.ultimoTiempoMilis = System.currentTimeMillis();
    }

    /**
     * Retorna una representación en string del vehículo.
     * 
     * <p>
     * Formato: "Movil [ID] [ETA: [valor] segs]"
     * </p>
     * 
     * @return string descriptivo del vehículo
     */
    @Override
    public String toString() {
        return "Movil " + id + " [ETA: " + String.format("%.1f", eta) + " segs]";
    }

    public void patrullar(GrafoSalta gs) {
        boolean atrapadoPuntoMuerto = false;

        if (this.getRutaAsignada() == null) {
            this.setRutaAsignada(new ArrayList<>());
        }

        // Llenar buffer de patrullaje (siempre miramos 3 esquinas al futuro)
        while (this.getRutaAsignada().size() < 3) {
            int nodoPunta;
            int nodoPrevioAlPunta;

            // Identificamos de dónde venimos para no volver hacia atrás
            if (this.getRutaAsignada().isEmpty()) {
                nodoPunta = this.getNodoActual();
                nodoPrevioAlPunta = this.nodoAnterior;
            } else {
                nodoPunta = this.getRutaAsignada().get(this.getRutaAsignada().size() - 1);
                nodoPrevioAlPunta = this.getRutaAsignada().size() >= 2
                        ? this.getRutaAsignada().get(this.getRutaAsignada().size() - 2)
                        : this.getNodoActual();
            }

            ArrayList<Integer> vecinos = gs.obtenerVecinosValidos(nodoPunta);

            if (!vecinos.isEmpty()) {

                // --- FILTRO ANTI PING-PONG (Evita giros en U) ---
                ArrayList<Integer> vecinosHaciaAdelante = new ArrayList<>();
                for (int v : vecinos) {
                    if (v != nodoPrevioAlPunta) {
                        vecinosHaciaAdelante.add(v);
                    }
                }

                int esquinaAzar;
                // Si hay calles para seguir avanzando, elige una al azar
                if (!vecinosHaciaAdelante.isEmpty()) {
                    esquinaAzar = vecinosHaciaAdelante.get((int) (Math.random() * vecinosHaciaAdelante.size()));
                } else {
                    // Solo si es un callejón sin salida (no le queda otra), da la vuelta
                    esquinaAzar = vecinos.get((int) (Math.random() * vecinos.size()));
                }

                this.getRutaAsignada().add(esquinaAzar);
            } else {
                atrapadoPuntoMuerto = true;
                break;
            }
        }

        // Sistema de rescate si cae en una isla completamente desconectada
        if (atrapadoPuntoMuerto && this.getRutaAsignada().isEmpty()) {
            int nodoRescate = (int) (Math.random() * gs.getOrden());
            Modelo.recursos.NodoMapa nRescate = gs.getNodo(nodoRescate);

            if (nRescate != null) {
                this.setNodoActual(nodoRescate);

                // ¡LA SOLUCIÓN AL AUTO VOLADOR!
                // Sincronizamos la física decimal con el nuevo nodo de rescate
                this.latActualDecimal = nRescate.getLatitud();
                this.lngActualDecimal = nRescate.getLongitud();

                this.nodoAnterior = -1; // Le borramos la memoria tras el rescate
                this.getRutaAsignada().clear();
                this.resetearRelojMecanico();
            }
            return;
        }

        // Ejecuta la cinemática vectorial suave que ya definimos
        if (!this.getRutaAsignada().isEmpty()) {
            int nodoObjetivoInmediato = this.getRutaAsignada().get(0);
            boolean cruzoEsquina = this.avanzarUnNodo(gs);
            if (cruzoEsquina) {
                this.setNodoActual(nodoObjetivoInmediato);
            }
        }
    }
}
