package es.us.lsi.hermes.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import es.us.lsi.hermes.domain.Event;
import es.us.lsi.hermes.domain.VehicleLocation;
import es.us.lsi.hermes.simulator.VisorController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    private static final Logger LOG = Logger.getLogger(VisorController.class.getName());

    /**
     * Method for loading a properties file.
     *
     * @param propertiesFileName Name of the properties file.
     * @return Loaded properties file.
     */
    public static Properties initProperties(String propertiesFileName) {
        Properties result = new Properties();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream(propertiesFileName);
            result.load(input);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "initProperties() - Error loading properties file: " + propertiesFileName, ex);
        } catch (NullPointerException ex) {
            LOG.log(Level.SEVERE, "initProperties() - File \'{0}\' not found", propertiesFileName);
        }

        return result;
    }

    /**
     * Método para obtener los eventos de Ztreamy recibidos. Como pueden llegar
     * eventos en forma de 'array' o de uno en uno, intentamos, en primer lugar,
     * la obtención como un único evento, que será el más habitual, y si
     * fallase, lo intentamos como 'array' de eventos.
     *
     * @param json JSON con el/los eventos recibidos.
     * @return Array con el/los evento/s obtenido/s del JSON.
     */
    public static Event[] getEventsFromJson(String json) {
        Event events[] = null;
        Gson gson = new Gson();

        // Comprobamos si llega un solo evento
        try {
            Event event = gson.fromJson(json, Event.class);
            events = new Event[]{event};
        } catch (JsonSyntaxException ex) {
            LOG.log(Level.SEVERE, "getEventsFromJson() - Error al intentar obtener un evento desde el JSON", ex.getMessage());
        }

        if (events == null) {
            try {
                events = gson.fromJson(json, Event[].class);
            } catch (JsonSyntaxException ex) {
                LOG.log(Level.SEVERE, "getEventsFromJson() - Error al intentar obtener un array de eventos desde el JSON", ex.getMessage());
            }
        }

        return events;
    }

    public static VehicleLocation getVehicleLocationFromEvent(Event event) {
        Gson gson = new Gson();

        try {
            return gson.fromJson(gson.toJson(event.getBody().get("Location")), VehicleLocation.class);
        } catch (JsonSyntaxException ex) {
            LOG.log(Level.SEVERE, "getVehicleLocationFromEvent() - Error al intentar obtener un 'Vehicle Location' de un evento", ex.getMessage());
        }

        return null;
    }
}
