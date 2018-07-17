package net.simforge.commons.legacy.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.io.FileInputStream;

public class Settings {

    private static final String SIMFORGE_PROPERTIES = "simforge.properties";
    private static Logger logger = LoggerFactory.getLogger(Settings.class);
    private static Properties properties;

    public static synchronized String get(String settingName) {
        if (properties == null) {
            load();
        }
        return (String) properties.get(settingName);
    }

    private static void load() {
        properties = new Properties();

        String simforgeSystemProperty = System.getProperty("simforge.settings");
        if (simforgeSystemProperty != null) {
            if (_loadFromPath(simforgeSystemProperty)) {
                return;
            }
        } else {
            logger.info("System property simforge.settings is not specified");
        }

        if (_loadFromPath(System.getProperty("user.home"))) {
            return;
        }

        if (_loadFromPath("./")) {
            return;
        }

        if (_loadFromResource()) {
            return;
        }

        logger.warn("Unable to find settings anywhere - using empty settings");
    }

    private static boolean _loadFromPath(String path) {
        try {
            InputStream inputStream = new FileInputStream(path + (path.endsWith("/") ? "" : "/") + SIMFORGE_PROPERTIES);
            properties.load(inputStream);
            logger.info("Settings loaded from path " + path);
            return true;
        } catch (Exception e) {
            logger.warn("Unable to find settings from path " + path);
            return false;
        }
    }

    private static boolean _loadFromResource() {
        try {
            InputStream inputStream = Settings.class.getResourceAsStream("/" + SIMFORGE_PROPERTIES);
            properties.load(inputStream);
            logger.info("Settings loaded from resources");
            return true;
        } catch (Exception e) {
            logger.warn("Unable to find settings in resources");
            return false;
        }
    }
}
