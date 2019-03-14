package de.domistiller.banker;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Banker {

    private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private BankerDatabase db;
    private Input input;

    private Properties settings;

    private Banker(Properties settings) {
        db = new BankerDatabase(
                settings.getProperty("dbserver"),
                settings.getProperty("dbuser"),
                settings.getProperty("dbpassword"));
        input = new Input();
    }

    private void start() {
        System.out.println("Welcome to Banker!");

        Input.MenuItem choice;
        do {
            choice = input.getMenuChoice();

            switch (choice) {
                case LIST_CUSTOMERS:
                    System.out.println("CUSTOMERS:");
                    listCustomers();
                    break;
                case CREATE_CUSTOMER:
                    System.out.println("CREATE NEW CUSTOMER:");
                    createCustomer();
                    break;
                case DELETE_CUSTOMER:
                    System.out.println("DELETE CUSTOMER:");
                    deleteCustomer();
                    break;
                case EXIT:
                    System.out.println("Goodbye from Banker!");
            }
        } while(choice != Input.MenuItem.EXIT);
    }

    private void listCustomers() {
        for(var c: db.getCustomers()) {
            System.out.print("   (" + c.getId() + ") ");
            System.out.print(c);
            System.out.println();
        }
    }

    private void createCustomer() {
        var c = input.getCustomer();
        var success = db.createCustomer(c);
        if (success) {
            System.out.println("Successfully created customer " + c.getName());
        } else {
            System.out.println("Could not create customer " + c.getName());
        }
    }

    private void deleteCustomer() {
        var id = input.getCustomerId();
        var success = db.deleteCustomer(id);
        if (success) {
            System.out.println("Successfully deleted customer with ID " + id);
        } else {
            System.out.println("Could not delete customer with ID " + id);
        }
    }

    public static void main(String[] args) {
        var settings = new Properties();
        // Try to read settings from file in cwd, then in jar
        try {
            settings.load(new FileInputStream("banker.properties"));
        } catch (FileNotFoundException e1) {
            try {
                settings.load(new BufferedReader(new InputStreamReader(Banker.class.getResourceAsStream("/banker.properties"))));
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
