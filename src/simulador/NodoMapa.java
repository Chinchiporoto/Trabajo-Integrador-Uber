public class NodoMapa{
protected long id;
protected double latitud,longitud;
protected String calleA,calleB, nombreEsquina;

public NodoMapa(){
    this.id=0;
    this.latitud=this.longitud=0.0;
    this.calleA=this.calleB=this.nombreEsquina="";
}
public NodoMapa(long ID, double Lat, double LONG, String c1, String c2, String esq){
    this.id=ID;
    this.latitud=Lat;
    this.longitud=LONG;
    this.calleA=c1;
    this.calleB=c2;
    this.nombreEsquina=esq;
}
public String getCalleA() {
    return calleA;
}
public String getCalleB() {
    return calleB;
}
public long getId() {
    return id;
}
public double getLatitud() {
    return latitud;
}
public double getLongitud() {
    return longitud;
}
public String getNombreEsquina(){
    return this.nombreEsquina;
}
public double distanciaHaversine(NodoMapa otro) {
double R = 63710000;

double lat1 = Math.toRadians(this.latitud);
double lat2 = Math.toRadians(otro.latitud);
double dLat = Math.toRadians(otro.latitud - this.latitud);
double dLng = Math.toRadians(otro.longitud - this.longitud);

double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
     + Math.cos(lat1) * Math.cos(lat2)
     * Math.sin(dLng / 2) * Math.sin(dLng / 2);

double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c; // metros
}
public double calcularETA(NodoMapa destino, String tipoVia){
      double metros = distanciaHaversine(destino);
    double velocidad = obtenerVelocidadMS(tipoVia);
    return metros / velocidad;
}

private double obtenerVelocidadMS(String tipoVia) {
    if (tipoVia == null) {
        return 5.0;
    }
    switch (tipoVia.toLowerCase()) {
        case "autopista":
        case "ruta":
            return 27.78; // 100 km/h in m/s
        case "avenida":
        case "carretera":
            return 16.67; // 60 km/h in m/s
        case "calle":
        case "residencial":
            return 8.33; // 30 km/h in m/s
        default:
            return 5.0; // default walking speed in m/s
    }
}
}