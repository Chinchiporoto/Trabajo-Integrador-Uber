package Modelo.recursos;

public final class MapaBounds {
    // Bounds del mapa visible (rendering)
    public static final double LAT_MIN = -24.805;
    public static final double LAT_MAX = -24.765;
    public static final double LNG_MIN = -65.430;
    public static final double LNG_MAX = -65.395;

    // Bounds de spawn — zona central con buena cobertura de calles
    public static final double SPAWN_LAT_MIN = -24.795;
    public static final double SPAWN_LAT_MAX = -24.775;
    public static final double SPAWN_LNG_MIN = -65.420;
    public static final double SPAWN_LNG_MAX = -65.405;

    private MapaBounds() {
    }
}