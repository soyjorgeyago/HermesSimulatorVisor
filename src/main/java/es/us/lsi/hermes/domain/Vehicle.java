package es.us.lsi.hermes.domain;

import es.us.lsi.hermes.domain.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Vehicle implements Serializable {

    private final String id;
    private int stress;
    private final Location[] historicLocations;
    private final SurroundingVehicle[] surroundingVehicles;
    private Integer oblivionTimeout;

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
        this.oblivionTimeout = 0;
        this.historicLocations = new Location[]{};
        this.surroundingVehicles = new SurroundingVehicle[]{};
    }

    public void decreaseOblivionTimeout() {
        oblivionTimeout = oblivionTimeout > 0 ? --oblivionTimeout : 0;
    }

    public Integer getOblivionTimeout() {
        return oblivionTimeout;
    }

    public Location getMostRecentHistoricLocationEntry() {
        return historicLocations.length == 0 ? null : historicLocations[historicLocations.length-1];
    }


    public String getId() {
        return id;
    }

    public int getStress() {
        return stress;
    }
    public void setStress(int stress) {
        this.stress = stress;
    }

    public class SurroundingVehicle {

        private String id;
        private int stress;
        private Double latitude;
        private Double longitude;

        private SurroundingVehicle(Vehicle v) {
            this.id = v.getId();
            this.stress = v.getStress();
            Location mrl = v.getMostRecentHistoricLocationEntry();
            this.latitude = mrl.getLatitude();
            this.longitude = mrl.getLongitude();
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

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

    }
}