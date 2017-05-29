package es.us.lsi.hermes.util;

import es.us.lsi.hermes.simulator.VisorController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    private static final Logger LOG = Logger.getLogger(VisorController.class.getName());

    private static final String MARKER_GREEN_CAR_ICON_PATH = "resources/img/greenCar.png";
    private static final String MARKER_YELLOW_CAR_ICON_PATH = "resources/img/yellowCar.png";
    private static final String MARKER_RED_CAR_ICON_PATH = "resources/img/redCar.png";

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

    public static int getIntValue(String propertyName, int defaultValue) {
        String property = VisorController.getKafkaProperties().getProperty(propertyName, String.valueOf(defaultValue));
        int value = defaultValue;

        try {
            value = Integer.parseInt(property);
        } catch (NumberFormatException ex) {
            LOG.log(Level.SEVERE, "validate() - Invalid value for {0}. Using default value: {1}", new Object[]{propertyName, defaultValue});
        }

        return value;
    }

    public static String getIconForStressLevel(int stressLevel){
        if (stressLevel > 80)
            return MARKER_RED_CAR_ICON_PATH;
        else if (stressLevel > 40)
            return MARKER_YELLOW_CAR_ICON_PATH;
        else
            return MARKER_GREEN_CAR_ICON_PATH;
    }
}
