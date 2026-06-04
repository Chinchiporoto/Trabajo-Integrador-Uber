package contenedores;

import simulador.Vehiculo;

public class VehiculoPriority extends ColaPrioridad{
    @Override
    public boolean esMenor(Object oa, Object ob){
        Vehiculo a = (Vehiculo) oa;
        Vehiculo b = (Vehiculo) ob;
        return a.getEta() < b.getEta();
    }

    @Override
    public boolean esMayor(Object oa, Object ob){
        Vehiculo a = (Vehiculo) oa;
        Vehiculo b = (Vehiculo) ob;
        return a.getEta() > b.getEta();
    }

    @Override
    public boolean iguales(Object oa, Object ob){
        Vehiculo a = (Vehiculo) oa;
        Vehiculo b = (Vehiculo) ob;
        return a.getEta() == b.getEta();
    }
}
