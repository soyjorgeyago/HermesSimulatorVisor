package es.us.lsi.hermes.domain;

import java.io.Serializable;

public class Vehicle implements  Serializable {

    private String id;
    private int stress;
    private Location lastLocation;

    /**
     * Constructor en el que se indicará el identificador del 'SmartDriver', el
     * máximo de ubicaciones pasadas que se analizarán y el número máximo de
     * otros 'SmartDrivers' que se tendrán en cuenta.
     *
     * @param id identificador del 'SmartDriver'
     * @param historySize Número máximo de ubicaciones que se tendrán en cuenta.
     */
    public Vehicle(String id, final Integer historySize) {
        this.id = id;
        this.stress = 0;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public int getStress() {
        return stress;
    }
    public void setStress(int stress) {
        this.stress = stress;
    }

    public Location getLastLocation() {
        return lastLocation;
    }
    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }
}