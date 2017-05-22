package es.us.lsi.hermes.simulator;

import es.us.lsi.hermes.util.MessageBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
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
    private static final LatLng SEVILLE = new LatLng(37.3898358, -5.986069);
    
    private static final String MARKER_GREEN_CAR_ICON_PATH = "resources/img/greenCar.png";
    private static final String MARKER_YELLOW_CAR_ICON_PATH = "resources/img/yellowCar.png";
    private static final String MARKER_RED_CAR_ICON_PATH = "resources/img/redCar.png";
    private static final String MARKER_START_ICON_PATH = "resources/img/home.png";

    private Marker marker;
    private static MapModel simulatedMapModel;

    @Inject
    @MessageBundle
    private ResourceBundle bundle;

    public VisorController() {
        simulatedMapModel = new DefaultMapModel();
    }

    @PostConstruct
    public void init() {
        LOG.log(Level.INFO, "init() - Visor init.");

        // TODO: Remove. This is only a visualization test.
        Marker m = new Marker(SEVILLE, bundle.getString("Info"), null, MARKER_GREEN_CAR_ICON_PATH);
        // TODO: Id from driver (SHA)
        //m.setId();
        m.setVisible(true);
        m.setDraggable(false);
        simulatedMapModel.addOverlay(m);

        // TODO: Center position to the first location obtained through REST or Consumer method.
        marker = new Marker(SEVILLE);
        marker.setDraggable(false);
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

    // TODO Consumer / REST method
}
