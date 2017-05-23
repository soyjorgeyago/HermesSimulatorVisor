package es.us.lsi.hermes.simulator;

import es.us.lsi.hermes.domain.Event;
import es.us.lsi.hermes.domain.VehicleLocation;
import es.us.lsi.hermes.util.MessageBundle;

import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import es.us.lsi.hermes.util.Utils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.primefaces.context.RequestContext;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

@Named("visorController")
@ApplicationScoped
public class VisorController {

    private static final Logger LOG = Logger.getLogger(VisorController.class.getName());

    // TODO: Remove and center position to the first location obtained through REST or Consumer method.
    private static final LatLng SEVILLE = new LatLng(37.3898350, -5.986100);
    private static final LatLng SEVILLE2 = new LatLng(37.3898400, -5.986150);
    private static final LatLng SEVILLE3 = new LatLng(37.3898450, -5.986200);

    private static final String MARKER_GREEN_CAR_ICON_PATH = "resources/img/greenCar.png";
    private static final String MARKER_YELLOW_CAR_ICON_PATH = "resources/img/yellowCar.png";
    private static final String MARKER_RED_CAR_ICON_PATH = "resources/img/redCar.png";
//    private static final String MARKER_START_ICON_PATH = "resources/img/home.png";

    private Marker marker;
    private static MapModel simulatedMapModel;
    private HashMap<String, Marker> markers;

    @Inject
    @MessageBundle
    private ResourceBundle bundle;

    public VisorController() {
        simulatedMapModel = new DefaultMapModel();
        markers = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        LOG.log(Level.INFO, "init() - Visor init.");

        // TODO: Center position to the first location obtained through REST or Consumer method.
        marker = new Marker(SEVILLE);
        marker.setDraggable(false);

//      FIXME Test Code 1
//        addToMap();

        initConsumer(5000);
    }

    public String getMarkerLatitudeLongitude() {
        if (marker != null) {
            return marker.getLatlng().getLat() + "," + marker.getLatlng().getLng();
        }

        return "";
    }

//      FIXME Test Code 2

//    public void updateFromInternet(){
//        LatLng aux = markers.get("0").getLatlng();
//        markers.get("0").setLatlng(markers.get("1").getLatlng());
//        markers.get("1").setLatlng(markers.get("2").getLatlng());
//        markers.get("2").setLatlng(aux);
//    }
//
//    private void addToMap(){
//        markers.put("0", new Marker(SEVILLE, "Hola", null, MARKER_GREEN_CAR_ICON_PATH));
//        markers.put("1", new Marker(SEVILLE2, "Hello", null, MARKER_RED_CAR_ICON_PATH));
//        markers.put("2", new Marker(SEVILLE3, "Halo", null, MARKER_YELLOW_CAR_ICON_PATH));
//
//        for (Marker marker : markers.values()) {
//            // TODO: Id from driver (SHA)
//            //marker.setId();
//            marker.setVisible(true);
//            marker.setDraggable(false);
//            simulatedMapModel.addOverlay(marker);
//        }
//    }

    public Marker getMarker() {
        return marker;
    }

    public MapModel getSimulatedMapModel() {
        return simulatedMapModel;
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        try {
            marker = (Marker) event.getOverlay();
            if (marker != null) {
                // TODO: Show relevant information.
            }
        } catch (ClassCastException ex) {

        }
    }

    public synchronized void updateMapGUI() {
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

    private static final String TOPIC_VEHICLE_LOCATION = "VehicleLocation";

    private KafkaConsumer<Long, String> consumer;
    private long pollTimeout;

    private void initConsumer(long pollTimeout) {
        this.pollTimeout = pollTimeout;
        consumer = new KafkaConsumer<>(Utils.initProperties("Kafka.properties"));
        consumer.subscribe(Collections.singletonList(TOPIC_VEHICLE_LOCATION));
    }

    public void updateFromInternet(){
        // The 'consumer' for each 'Vehicle Locations' will poll every 'pollTimeout' milisegundos, to get all the data received by Kafka.
        for (ConsumerRecord<Long, String> record : consumer.poll(pollTimeout)) {
            // Get the data since the last poll and process it
            Event events[] = Utils.getEventsFromJson(record.value());
            if (events == null || events.length <= 0) {
                LOG.log(Level.SEVERE, "VehicleLocationConsumer.doWork() - Error obtaining 'Vehicle Location' events from the JSON received: {0}", record.value());
                continue;
            }

            for (Event event : events) {
                LOG.log(Level.FINE, "VehicleLocationConsumer.doWork() - VEHICLE LOCATION {0} with event-id {1}", new Object[]{event.getTimestamp(), event.getEventId()});

                // Get the vehicle location.
                VehicleLocation vehicleLocation = Utils.getVehicleLocationFromEvent(event);
                if (vehicleLocation == null)
                    continue;

                // A 'HashMap' will store the vehicles with the most updated information, in essence those who are moving
                Marker analyzedVehicle = markers.get(event.getSourceId());
                LatLng latLng = new LatLng(vehicleLocation.getLatitude(), vehicleLocation.getLongitude());
                int stress = vehicleLocation.getStress();

                // After getting searching for the vehicle with its sourceId, process and store in case it doesn't exists
                if (analyzedVehicle == null) {
                    System.out.println("NUEVO");
                    Marker marker = new Marker(latLng, "Id: " + event.getSourceId(), null, getIconForStressLevel(stress));
                    marker.setId(marker.getId());
                    marker.setVisible(true);
                    marker.setDraggable(false);
                    markers.put(event.getSourceId(), marker);
                    simulatedMapModel.addOverlay(markers.get(marker.getId()));
                } else {
                    System.out.println("UPDATE");
                    markers.get(event.getSourceId()).setLatlng(latLng);
                    markers.get(event.getSourceId()).setIcon(getIconForStressLevel(stress));
                }
            }
        }
    }

    private String getIconForStressLevel(int stressLevel){
        if (stressLevel > 80)
            return MARKER_RED_CAR_ICON_PATH;
        else if (stressLevel > 40)
            return MARKER_YELLOW_CAR_ICON_PATH;
        else
            return MARKER_GREEN_CAR_ICON_PATH;
    }
}
