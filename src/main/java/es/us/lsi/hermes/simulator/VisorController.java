package es.us.lsi.hermes.simulator;

import es.us.lsi.hermes.domain.Location;
import es.us.lsi.hermes.domain.Vehicle;
import es.us.lsi.hermes.kafka.ActiveVehiclesConsumer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import es.us.lsi.hermes.util.Utils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

@Named("visorController")
@ApplicationScoped
public class VisorController implements IControllerObserver {

    private static final Logger LOG = Logger.getLogger(VisorController.class.getName());

    // TODO: Remove and center position to the first location obtained through REST or Consumer method.
    private static final LatLng SEVILLE = new LatLng(37.3898350, -5.986100);

    private static final Properties KAFKA_PROPERTIES = Utils.initProperties("Kafka.properties");

    private Marker marker;
    private static MapModel simulatedMapModel;
    private final static HashMap<String, Marker> markers = new HashMap<>();

//    @Inject
//    @MessageBundle
//    private ResourceBundle bundle;

    public VisorController() { }

    @PostConstruct
    public void init() {
        LOG.log(Level.INFO, "init() - Visor init.");

        simulatedMapModel = new DefaultMapModel();
        marker = new Marker(SEVILLE);
        marker.setDraggable(false);
        marker.setVisible(false);
        simulatedMapModel.addOverlay(marker);

        //FIXME
        System.out.println("CONSUMER CREATION");
        ActiveVehiclesConsumer consumer = new ActiveVehiclesConsumer(this);
        consumer.start();
    }

    public String getMarkerLatitudeLongitude() {
        if (marker != null) {
            return marker.getLatlng().getLat() + "," + marker.getLatlng().getLng();
        }

        return "";
    }

    public Marker getMarker() {
        return marker;
    }

    public MapModel getSimulatedMapModel() {
        return simulatedMapModel;
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        if(event.getOverlay() instanceof Marker) {
            // TODO: Show relevant information.
        }
    }

    public void updateMapGUI() {

        RequestContext context = RequestContext.getCurrentInstance();
        for (Marker m : simulatedMapModel.getMarkers()) {
            LOG.log(Level.FINE, "updateMapGUI() - Marker id: {0}", m.getId());

            LatLng latLng = m.getLatlng();
            // Location.
            if (latLng != null) {
                context.addCallbackParam("latLng_" + m.getId(), latLng.getLat() + "," + latLng.getLng());
            }
            // Icon.
            String icon = m.getIcon();
            if (icon != null) {
                context.addCallbackParam("icon_" + m.getId(), icon);
            }
            // Information.
            String title = m.getTitle();
            if (title != null) {
                context.addCallbackParam("title_" + m.getId(), title);
            }
        }
    }

    // -------------------------- CONSUMER ---------------------------

    @Override
    public void update(Vehicle[] activeVehicles) {
        Set<String> oldKeys = new HashSet<>(markers.keySet());

        if(activeVehicles == null) {
            //FIXME
            System.out.println("removing ALL keys");
            removeFromMarkers(oldKeys);
            return;
        }

        for(Vehicle vehicle : activeVehicles){
            Marker analyzedVehicle = markers.get(vehicle.getId());
            Location currentLocation = vehicle.getLastLocation();
            if(currentLocation == null){
                LOG.log(Level.SEVERE, "The vehicle set has no locations");
                continue;
            }
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            String stressBasedIcon = Utils.getIconForStressLevel(vehicle.getStress());

            //FIXME
            System.out.println("SIZE: " + markers.keySet().size());

            // If the vehicle is new, add a new marker
            if (analyzedVehicle == null) {
                //FIXME
                System.out.println("NUEVO");
                Marker m = new Marker(latLng, "Id: " + vehicle.getId(), null, stressBasedIcon);
                m.setId(vehicle.getId());
                m.setVisible(true);
                m.setDraggable(false);
                markers.put(vehicle.getId(), m);
                simulatedMapModel.addOverlay(m);

            // If the vehicle has an assigned marker, update the location and remove the key from the outdated ones
            } else {
                //FIXME
                System.out.println("UPDATE");
                markers.get(vehicle.getId()).setLatlng(latLng);
                markers.get(vehicle.getId()).setIcon(stressBasedIcon);
                oldKeys.remove(vehicle.getId());
            }
        }

        removeFromMarkers(oldKeys);
    }

    private static void removeFromMarkers(Set<String> oldKeys){
        // Delete the markers that doesn't match with an active vehicle
        for(String key : oldKeys){
            //FIXME
            System.out.println("REMOVING");
            markers.remove(key);
        }
    }

    public static Properties getKafkaProperties() {
        return KAFKA_PROPERTIES;
    }

    public int getVehicleCounter() {
        return markers.size();
    }
}
