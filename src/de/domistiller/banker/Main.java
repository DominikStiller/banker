/**
 * Launch code which starts actual program
 */

package de.domistiller.banker;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) {
        Properties settings = new Properties();

        // Try to read settings from file in current directory
        try {
            settings.load(new FileInputStream("banker.properties"));
        } catch (FileNotFoundException e1) {
            // Try to read settings from file from jar
            try {
                InputStream file = Banker.class.getResourceAsStream("/banker.properties");
                if (file == null) {
                    System.out.println("Config file \"banker.properties\" not found");
                    System.exit(-1);
                }

                settings.load(new BufferedReader(new InputStreamReader(file)));
            } catch (IOException e) {
                log.log(Level.SEVERE, "error reading config", e);
                System.exit(-1);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "error reading config", e);
            System.exit(-1);
        }

        // Set log level based on config
        if(settings.getProperty("debug").equals("true")) {
            log.setLevel(Level.INFO);
        } else {
            log.setLevel(Level.SEVERE);
        }

        log.info("settings loaded");

        new Banker(settings).start();
    }
}
