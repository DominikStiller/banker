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
        var settings = new Properties();
        // Try to read settings from file in cwd, then in jar
        try {
            settings.load(new FileInputStream("banker.properties"));
        } catch (FileNotFoundException e1) {
            try {
                settings.load(new BufferedReader(
                        new InputStreamReader(Banker.class.getResourceAsStream("/banker.properties"))));
            } catch (FileNotFoundException e2) {
                System.out.println("Config file \"banker.properties\" not found");
                System.exit(-1);
            } catch (IOException e) {
                log.log(Level.SEVERE, "error reading config", e);
                System.exit(-1);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "error reading config", e);
            System.exit(-1);
        }

        if(settings.getProperty("debug").equals("true")) {
            log.setLevel(Level.INFO);
        } else {
            log.setLevel(Level.SEVERE);
        }

        new Banker(settings).start();
    }
}
