package de.domistiller.banker;

import de.domistiller.banker.model.Account;
import de.domistiller.banker.model.Amount;
import de.domistiller.banker.model.Customer;
import de.domistiller.banker.model.Transfer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles all database interaction
 * Contains read and write queries
 */
public class Database implements AutoCloseable {

    private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private Connection conn;

    private PreparedStatement getCustomer;
    private PreparedStatement getCustomers;
    private PreparedStatement createCustomer;
    private PreparedStatement deleteCustomer;

    private PreparedStatement getAccount;
    private PreparedStatement getAccounts;
    private PreparedStatement getAccountBalance;
    private PreparedStatement createAccount;
    private PreparedStatement deleteAccount;

    private PreparedStatement getTransfers;
    private PreparedStatement makeTransfer;

    public Database(String server, String user, String password) {
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
        // See sql/queries.sql for SQL-only code
        getCustomer = conn.prepareStatement("SELECT * FROM customers WHERE id = ?");
        getCustomers = conn.prepareStatement("SELECT * FROM customers");
        createCustomer = conn.prepareStatement(
                "INSERT INTO customers (name, address, email, phone) VALUES (?, ?, ?, ?)");
        deleteCustomer = conn.prepareStatement("DELETE FROM customers WHERE id = ?");

        getAccount = conn.prepareStatement(
                "SELECT *\n" +
                "FROM accounts\n" +
                "WHERE customer_id = ?\n" +
                "AND account_no = ?");
        getAccounts = conn.prepareStatement("SELECT * FROM accounts WHERE customer_id = ?");
        getAccountBalance = conn.prepareStatement(
                    "SELECT initial_balance\n" +
                    "+ (SELECT IFNULL(SUM(x.amount * x.rate), 0)\n" +
                    "   FROM (SELECT t.amount, (SELECT rate\n" +
                    "                           FROM exchangerates\n" +
                    "                           WHERE from_currency = t.currency\n" +
                    "                           AND to_currency = (SELECT currency\n" +
                    "                                                FROM accounts\n" +
                    "                                                WHERE customer_id = ?\n" +
                    "                                                AND account_no = ?)\n" +
                    "                           AND date <= t.date\n" +
                    "                           ORDER BY date DESC\n" +
                    "                           LIMIT 1) as rate\n" +
                    "         FROM transfers t\n" +
                    "         WHERE receiver_id = ?\n" +
                    "         AND receiver_account = ?) as x)\n" +
                    "- (SELECT IFNULL(SUM(x.amount * x.rate), 0)\n" +
                    "   FROM (SELECT t.amount, (SELECT rate\n" +
                    "                           FROM exchangerates\n" +
                    "                           WHERE from_currency = t.currency\n" +
                    "                           AND to_currency = (SELECT currency\n" +
                    "                                                FROM accounts\n" +
                    "                                                WHERE customer_id = ?\n" +
                    "                                                AND account_no = ?)\n" +
                    "                           AND date <= t.date\n" +
                    "                           ORDER BY date DESC\n" +
                    "                           LIMIT 1) as rate\n" +
                    "         FROM transfers t\n" +
                    "         WHERE sender_id = ?\n" +
                    "         AND sender_account = ?) as x) as balance, currency\n" +
                    "FROM accounts\n" +
                    "WHERE customer_id = ?\n" +
                    "AND account_no = ?"
        );
        createAccount = conn.prepareStatement(
                "INSERT INTO accounts (customer_id, account_no, type, currency, initial_balance) \n" +
                "VALUES (?, ?, ?, ?, ?)");
        deleteAccount = conn.prepareStatement("DELETE FROM accounts where customer_id = ? AND account_no = ?");

        getTransfers = conn.prepareStatement(
                "SELECT *\n" +
                    "FROM transfers\n" +
                    "WHERE (sender_id = ? AND sender_account = ?)\n" +
                    "OR (receiver_id = ? AND receiver_account = ?)\n" +
                    "ORDER BY date ASC"
        );
        // TODO makeTransfer
    }

    Customer getCustomer(int id) {
        try {
            getCustomer.setInt(1, id);

            try (var rs = getCustomer.executeQuery()) {
                if(rs.next()) {
                    return customerFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error fetching customer", e);
        }
        return null;
    }

    List<Customer> getCustomers() {
        var list = new ArrayList<Customer>();
        try (var rs = getCustomers.executeQuery()) {
            while(rs.next()) {
                list.add(customerFromResultSet(rs));
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error fetching customers", e);
        }
        return list;
    }

    private Customer customerFromResultSet(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5));
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

    Account getAccount(Account.Reference ref) {
        try {
            getAccount.setInt(1, ref.getCustomerId());
            getAccount.setInt(2, ref.getAccountNumber());

            try (var rs = getAccount.executeQuery()) {
                if (rs.next()) {
                    return accountFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error fetching account", e);
        }
        return null;
    }

    List<Account> getAccounts(int customerId) {
        var list = new ArrayList<Account>();
        try {
            getAccounts.setInt(1, customerId);

            try (var rs = getAccounts.executeQuery()) {
                while(rs.next()) {
                    list.add(accountFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error fetching accounts", e);
        }
        return list;
    }

    private Account accountFromResultSet(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt(1),
                rs.getInt(2),
                Account.Type.fromString(rs.getString(3)),
                rs.getString(4),
                rs.getDouble(5));
    }

    Amount getAccountBalance(Account.Reference ref) {
        try {
            getAccountBalance.setInt(1 , ref.getCustomerId());
            getAccountBalance.setInt(2 , ref.getAccountNumber());
            getAccountBalance.setInt(3 , ref.getCustomerId());
            getAccountBalance.setInt(4 , ref.getAccountNumber());
            getAccountBalance.setInt(5 , ref.getCustomerId());
            getAccountBalance.setInt(6 , ref.getAccountNumber());
            getAccountBalance.setInt(7 , ref.getCustomerId());
            getAccountBalance.setInt(8 , ref.getAccountNumber());
            getAccountBalance.setInt(9 , ref.getCustomerId());
            getAccountBalance.setInt(10 , ref.getAccountNumber());

            try (var rs = getAccountBalance.executeQuery()) {
                if (rs.next()) {
                    return new Amount(rs.getDouble(1), rs.getString(2));
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error fetching account balance", e);
        }
        return null;
    }

    boolean createAccount(Account a) {
        try {
            createAccount.setInt(1, a.getRef().getCustomerId());
            createAccount.setInt(2, a.getRef().getAccountNumber());
            createAccount.setString(3, a.getType().name().toLowerCase());
            createAccount.setString(4, a.getInitialBalance().getCurrency());
            createAccount.setDouble(5, a.getInitialBalance().getAmount());

            var result = createAccount.executeUpdate();
            return result == 1;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error creating account", e);
            return false;
        }
    }

    boolean deleteAccount(Account.Reference a) {
        try {
            deleteAccount.setInt(1, a.getCustomerId());
            deleteAccount.setInt(2, a.getAccountNumber());
            var result = deleteAccount.executeUpdate();
            return result == 1;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error deleting account", e);
            return false;
        }
    }


    List<Transfer> getTransfers(Account.Reference ref) {
        var list = new ArrayList<Transfer>();
        try {
            getTransfers.setInt(1, ref.getCustomerId());
            getTransfers.setInt(2, ref.getAccountNumber());
            getTransfers.setInt(3, ref.getCustomerId());
            getTransfers.setInt(4, ref.getAccountNumber());

            try (var rs = getTransfers.executeQuery()) {
                while(rs.next()) {
                    list.add(new Transfer(
                            rs.getInt(1),
                            new Account.Reference(rs.getInt(2), rs.getInt(3)),
                            new Account.Reference(rs.getInt(4), rs.getInt(5)),
                            new Amount(rs.getDouble(6), rs.getString(7)),
                            rs.getTimestamp(8),
                            rs.getString(9)
                    ));
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error fetching transfers", e);
        }
        return list;
    }

    @Override
    public void close() {
        try {
            getCustomer.close();
            getCustomers.close();
            createCustomer.close();
            deleteCustomer.close();

            getAccount.close();
            getAccounts.close();
            getAccountBalance.close();
            createAccount.close();
            deleteAccount.close();

            getTransfers.close();
            makeTransfer.close();

            conn.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "error when closing connection", e);
        }
    }
}
