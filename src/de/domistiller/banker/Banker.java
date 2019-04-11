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
                settings.getProperty("dbpassword"),
                settings.getProperty("dbname"));
        input = new Input(db);
    }

    public void start() {
        System.out.println("Welcome to Banker!");

        Input.MenuChoice choice;

        // Main loop
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
                case LIST_ACCOUNTS:
                    System.out.println("LIST ACCOUNTS:");
                    listAccounts();
                    break;
                case CREATE_ACCOUNT:
                    System.out.println("CREATE NEW ACCOUNT:");
                    createAccount();
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
        } while(choice != Input.MenuChoice.EXIT);

        db.close();
    }

    private void listCustomers() {
        System.out.println();
        System.out.println("ID   NAME                     NO. OF ACCOUNTS     EMAIL                              PHONE             ADDRESS");
        printSeparator();

        var customers = db.getCustomers();
        for (Customer c : customers) {
            System.out.printf(
                    "%-4d %-20s     %-15d     %-30s     %-13s     %s\n",
                    c.getId(),
                    c.getName(),
                    c.getNumAcounts(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getAddress()
            );
        }

        System.out.println();
        System.out.println(customers.size() + " customers found");
    }

    private void createCustomer() {
        var customer = input.getNewCustomer();
        var success = db.createCustomer(customer);

        System.out.println();
        if (success) {
            System.out.println("Successfully created customer " + customer.getName());
        } else {
            System.out.println("Could not create customer " + customer.getName());
        }
    }

    private void listAccounts() {
        var customerId = input.getCustomerId();
        var customer = db.getCustomer(customerId);

        System.out.println();
        System.out.println("ACCOUNTS FOR CUSTOMER " + customer.getName() + " (ID " + customer.getId() + ")\n");
        printSeparator();
        System.out.println("ACCOUNT NO.      INITIAL BALANCE      CURRENT BALANCE");
        printSeparator();

        var accounts = db.getAccounts(customerId);
        for (Account a : accounts) {
            System.out.printf(
                    "%-11s      %-15s      %-15s\n",
                    a.getRef(),
                    a.getInitialBalance(),
                    db.getAccountBalance(a.getRef())
            );
        }

        System.out.println();
        System.out.println(accounts.size() + " accounts found");
    }

    private void createAccount() {
        var account = input.getNewAccount();
        var success = db.createAccount(account);

        System.out.println();

        if (success) {
            System.out.println("Successfully created account " + account.getRef());
        } else {
            System.out.println("Could not create account");
        }
    }

    private void showBankStatement() {
        var accountRef = input.getExistingAccountRef();
        var initialBalance = db.getAccount(accountRef).getInitialBalance();
        var totalBalance = db.getAccountBalance(accountRef);

        System.out.println();
        System.out.println("TRANSFERS FOR ACCOUNT " + accountRef);
        printSeparator();
        System.out.println("DATE     TIME      AMOUNT              SENDER                           RECEIVER                           REFERENCE");
        printSeparator();

        for (Transfer t : db.getTransfers(accountRef)) {
            System.out.printf(
                    "%tD %tR     %c %9.2f %s     %-30s   %-30s     %-100s\n",
                    t.getExecutionDate(),
                    t.getExecutionDate(),
                    t.getSignFor(accountRef),
                    t.getAmount().getAmount(),
                    t.getAmount().getCurrency(),
                    t.getSenderName() + " (" + t.getSender() + ")",
                    t.getReceiverName() + " (" + t.getReceiver() + ")",
                    t.getReference()
            );
        }

        printSeparator();
        System.out.printf("INITIAL BALANCE:   %s %9.2f %s\n",
                initialBalance.getSign(),
                initialBalance.toAbsolute().getAmount(),
                initialBalance.getCurrency());

        printSeparator();
        System.out.printf("TOTAL BALANCE:     %s %9.2f %s\n",
                totalBalance.getSign(),
                totalBalance.toAbsolute().getAmount(),
                totalBalance.getCurrency());
    }

    private void makeTransfer() {
        var transfer = input.getNewTransfer();

        var senderAccountBalance = db.getAccountBalance(transfer.getSender());
        var senderAccountBalanceInTransferCurrency =
                db.convertCurrency(senderAccountBalance, transfer.getAmount().getCurrency());

        System.out.println();

        // Only allow transfer if sender's account balance is sufficient
        if (senderAccountBalanceInTransferCurrency.getAmount() < transfer.getAmount().getAmount()) {
            System.out.println("Sender account does not have sufficient funds.");
            System.out.println("Sender account balance: " + senderAccountBalance);
            System.out.println("Transfer amount: " + transfer.getAmount());
            System.out.println("Missing amount: " + senderAccountBalanceInTransferCurrency.minus(transfer.getAmount()));
            return;
        }

        var success = db.makeTransfer(transfer);
        if (success) {
            System.out.println("Successfully made wire transfer");
            System.out.println();
            System.out.println("TRANSFER DETAILS");
            System.out.printf("Date: %tD %tR\n",
                    transfer.getExecutionDate(), transfer.getExecutionDate());
            System.out.println("Sender: " + db.getCustomer(transfer.getSender().getCustomerId()).getName()
                    + " (Account " + transfer.getSender() + ")");
            System.out.println("Receiver: " + db.getCustomer(transfer.getReceiver().getCustomerId()).getName()
                    + " (Account " + transfer.getReceiver() + ")");
            System.out.println("Amount: " + transfer.getAmount());
            System.out.println("Reference: " + transfer.getReference());
        } else {
            System.out.println("Could not make wire transfer");
        }
    }

    private void printSeparator() {
        printSeparator('-');
    }

    private void printSeparator(char character) {
        var s = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            s.append(character);
        }
        System.out.println(s);
    }
}
