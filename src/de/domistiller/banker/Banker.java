package de.domistiller.banker;

import de.domistiller.banker.model.Account;
import de.domistiller.banker.model.Customer;
import de.domistiller.banker.model.Transfer;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Contains main loop and top-level functionality
 */
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
        input = new Input(db);
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
                case CREATE_ACCOUNT:
                    System.out.println("CREATE NEW ACCOUNT:");
                    createAccount();
                    break;
                case DELETE_ACCOUNT:
                    System.out.println("DELETE ACCOUNT:");
                    deleteAccount();
                    break;
                case SHOW_BANK_STATEMENT:
                    System.out.println("BANK STATEMENT:");
                    showBankStatement();
                    break;
                case MAKE_TRANSFER:
                    System.out.println("MAKE WIRE TRANSFER:");
                    makeTransfer();
                    break;
                case EXIT:
                    System.out.println("Goodbye from Banker!");
            }
            System.out.println("\n\n");
            printSeparator('#');
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
        var id = input.getCustomerId();
        System.out.println();

        var customer = db.getCustomer(id);
        if (customer == null) {
            System.out.println("Customer not found");
        }

        var success = db.deleteCustomer(id);
        if (success) {
            System.out.println("Successfully deleted customer " + customer.getName() + " (ID " + customer.getId());
        } else {
            System.out.println("Could not delete customer " + customer.getName() + " (ID " + customer.getId());
        }
    }

    private void listAccounts() {
        var id = input.getCustomerId();
        System.out.println();

        var customer = db.getCustomer(id);
        if (customer == null) {
            System.out.println("Customer not found");
            return;
        }

        System.out.println("ACCOUNTS FOR CUSTOMER " + customer.getName() + " (ID " + customer.getId() + ")\n");
        printSeparator();
        System.out.println("ACCOUNT NO.     TYPE           INITIAL BALANCE      CURRENT BALANCE");
        printSeparator();
        for (Account a : db.getAccounts(id)) {
            System.out.printf(
                    "%-11s     %-10s     %-15s      %-15s\n",
                    a.getRef(),
                    a.getType(),
                    a.getInitialBalance(),
                    db.getAccountBalance(a.getRef())
            );
        }
    }

    private void createAccount() {
        var a = input.getNewAccount();

        if (a == null) {
            return;
        }

        var success = db.createAccount(a);
        if (success) {
            System.out.println("Successfully created account " + a.getRef());
        } else {
            System.out.println("Could not create account");
        }
    }

    private void deleteAccount() {
        var ref = input.getAccountRef();
        System.out.println();

        var success = db.deleteAccount(ref);
        if (success) {
            System.out.println("Successfully deleted account " + ref);
        } else {
            System.out.println("Could not delete account " + ref);
        }
    }

    private void showBankStatement() {
        var ref = input.getAccountRef();

        if (ref == null) {
            return;
        }
        System.out.println();

        var account = db.getAccount(ref);

        if (account == null) {
            System.out.println("Account not found");
            return;
        }

        var initialBalance = account.getInitialBalance();
        var totalBalance = db.getAccountBalance(ref);

        System.out.println("TRANSFERS FOR ACCOUNT " + account.getRef());
        printSeparator();
        System.out.println("DATE     TIME       AMOUNT             SENDER    RECEIVER    REFERENCE");
        printSeparator();
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
        printSeparator();
        System.out.printf("INITIAL BALANCE:   %s %9.2f %s\n",
                initialBalance.getAmount() >= 0 ? "+" : "-",
                initialBalance.toAbsolute().getAmount(),
                initialBalance.getCurrency());
        printSeparator();
        System.out.printf("TOTAL BALANCE:     %s %9.2f %s\n\n",
                totalBalance.getAmount() >= 0 ? "+" : "-",
                totalBalance.toAbsolute().getAmount(),
                totalBalance.getCurrency());
    }

    private void makeTransfer() {

    }

    private void printSeparator() {
        printSeparator('-');
    }

    private void printSeparator(char character) {
        var s = new StringBuilder();
        for (int i = 0; i < 115; i++) {
            s.append(character);
        }
        System.out.println(s);
    }
}
