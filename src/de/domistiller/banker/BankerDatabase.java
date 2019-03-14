package de.domistiller.banker;

import de.domistiller.banker.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BankerDatabase implements AutoCloseable {

    private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private Connection conn;
    private PreparedStatement getCustomers;
    private PreparedStatement createCustomer;
    private PreparedStatement deleteCustomer;

    public BankerDatabase(String server, String user, String password) {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://" + server + "/banker?user=" + user + "&password=" + password);
            log.info("database connection established");
        } catch (SQLException e) {
            log.log(Level.SEVERE, "connection error", e);
            System.exit(-1);
        }
        try {
            initializeStatements();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error creating SQL statements", e);
            System.exit(-1);
        }
    }

    private void initializeStatements() throws SQLException {
        getCustomers = conn.prepareStatement("SELECT * FROM customers");
        createCustomer = conn.prepareStatement(
                "INSERT INTO customers (name, address, email, phone) VALUES (?, ?, ?, ?)");
        deleteCustomer = conn.prepareStatement("DELETE FROM customers WHERE id = ?");
    }

    List<Customer> getCustomers() {
        var list = new ArrayList<Customer>();
        try (var rs = getCustomers.executeQuery()) {
            while(rs.next()) {
                list.add(new Customer(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5)));
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error fetching customers", e);
        }
        return list;
    }

    boolean createCustomer(Customer c) {
        try {
            createCustomer.setString(1, c.getName());
            createCustomer.setString(2, c.getAddress());
            createCustomer.setString(3, c.getEmail());
            createCustomer.setString(4, c.getPhone());

            var result = createCustomer.executeUpdate();
            return result == 1;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error creating customer", e);
            return false;
        }
    }

    boolean deleteCustomer(int id) {
        try {
            deleteCustomer.setInt(1, id);
            var result = deleteCustomer.executeUpdate();
            return result == 1;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error deleting customer", e);
            return false;
        }
    }

    @Override
    public void close() {
        try {
            getCustomers.close();
            createCustomer.close();
            deleteCustomer.close();

            conn.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error when closing connection", e);
        }
    }
}
