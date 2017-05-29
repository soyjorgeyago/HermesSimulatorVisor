package es.us.lsi.hermes.simulator;

import es.us.lsi.hermes.domain.Vehicle;

public interface IControllerObserver {

    void update(Vehicle[] activeVehicles);
}
