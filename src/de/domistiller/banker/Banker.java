/**
 * TODO Add description to all classes
 */
package de.domistiller.banker;

import de.domistiller.banker.model.Account;
import de.domistiller.banker.model.Customer;
import de.domistiller.banker.model.Transfer;

import java.util.Properties;
import java.util.logging.Logger;

public class Banker {

    private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private Database db;
    private Input input;

    private Properties settings;

    public Banker(Properties settings) {
        this.settings = settings;

        db = new Database(
                settings.getProperty("dbserver"),
                settings.getProperty("dbuser"),
                settings.getProperty("dbpassword"));
        input = new Input();
    }

    public void start() {
        System.out.println("Welcome to Banker!");

        Input.MenuItem choice;
        do {
            choice = input.getMenuChoice();

            switch (choice) {
                case LIST_CUSTOMERS:
                    System.out.println("LIST CUSTOMERS:");
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
                case LIST_ACCOUNTS:
                    System.out.println("LIST ACCOUNTS:");
                    listAccounts();
                    break;
                case LIST_TRANSFERS:
                    System.out.println("LIST TRANSFERS:");
                    listTransfers();
                    break;
                case EXIT:
                    System.out.println("Goodbye from Banker!");
            }
            System.out.println("\n\n");
        } while(choice != Input.MenuItem.EXIT);
    }

    private void listCustomers() {
        System.out.println();
        System.out.println("ID   NAME                     EMAIL                              PHONE             ADDRESS");
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        for (Customer c : db.getCustomers()) {
            System.out.printf(
                    "%-4d %-20s     %-30s     %-13s     %s\n",
                    c.getId(),
                    c.getName(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getAddress()
            );
        }
    }

    private void createCustomer() {
        var c = input.getNewCustomer();
        var success = db.createCustomer(c);
        if (success) {
            System.out.println("Successfully created customer " + c.getName());
        } else {
            System.out.println("Could not create customer " + c.getName());
        }
    }

    private void deleteCustomer() {
        var id = input.getCustomerId(this::listCustomersSimple);
        var success = db.deleteCustomer(id);
        if (success) {
            System.out.println("Successfully deleted customer with ID " + id);
        } else {
            System.out.println("Could not delete customer with ID " + id);
        }
    }

    private void listAccounts() {
        var id = input.getCustomerId(this::listCustomersSimple);
        var customer = db.getCustomer(id);

        if (customer == null) {
            System.out.println("Customer not found");
            return;
        }

        System.out.println("ACCOUNTS FOR CUSTOMER " + customer.getName() + "(ID " + customer.getId() + ")\n");
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("ACCOUNT NO.     TYPE           INITIAL BALANCE      CURRENT BALANCE");
        System.out.println("---------------------------------------------------------------------------------");
        for (Account a : db.getAccounts(id)) {
            System.out.printf(
                    "%-11s     %-20s     %-15s      %-15s\n",
                    a.getRef(),
                    a.getType(),
                    a.getInitialBalance(),
                    db.getAccountBalance(a.getRef())
            );
        }
    }

    private void listTransfers() {
        var ref = input.getAccountRef(this::listCustomersSimple, this::listAccountsSimple);

        if (ref == null) {
            return;
        }

        var account = db.getAccount(ref);

        if (account == null) {
            System.out.println("Account not found");
            return;
        }

        var initialBalance = account.getInitialBalance();
        var totalBalance = db.getAccountBalance(ref);

        System.out.println("TRANSFERS FOR ACCOUNT " + account.getRef());
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("DATE     TIME       AMOUNT             SENDER    RECEIVER    REFERENCE");
        System.out.println("---------------------------------------------------------------------------------");
        for (Transfer t : db.getTransfers(ref)) {
            System.out.printf(
                    "%tD %tR     %s %9.2f %s     %-7s   %-7s     %-100s\n",
                    t.getDate(),
                    t.getDate(),
                    t.isIncomingFor(ref) ? "+" : '-',
                    t.getAmount().getAmount(),
                    t.getAmount().getCurrency(),
                    t.getSender(),
                    t.getReceiver(),
                    t.getReference()
            );
        }
        System.out.println("---------------------------------------------------------------------------------");
        System.out.printf("INITIAL BALANCE:   %s %9.2f %s\n",
                initialBalance.getAmount() >= 0 ? "+" : "-",
                initialBalance.toAbsolute().getAmount(),
                initialBalance.getCurrency());
        System.out.println("---------------------------------------------------------------------------------");
        System.out.printf("TOTAL BALANCE:     %s %9.2f %s\n\n",
                totalBalance.getAmount() >= 0 ? "+" : "-",
                totalBalance.toAbsolute().getAmount(),
                totalBalance.getCurrency());
    }

    // For passing to input class
    private void listCustomersSimple() {
        System.out.println("CUSTOMERS:");
        System.out.println("ID   NAME");
        for (Customer c : db.getCustomers()) {
            System.out.printf("%-2d   %s\n", c.getId(), c.getName());
        }
    }

    private boolean listAccountsSimple(int customerId) {
        var customer = db.getCustomer(customerId);

        if (customer == null) {
            return false;
        }

        System.out.println("ACCOUNTS FOR CUSTOMER " + customer.getName());
        System.out.println("NO.   TYPE        CURRENCY");
        for (Account a : db.getAccounts(customerId)) {
            System.out.printf("%-3d   %-9s   %s\n",
                    a.getRef().getAccountNumber(),
                    a.getType(),
                    a.getInitialBalance().getCurrency()
            );
        }

        return true;
    }
}
