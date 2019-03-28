package de.domistiller.banker;

import de.domistiller.banker.model.Account;
import de.domistiller.banker.model.Amount;
import de.domistiller.banker.model.Customer;
import de.domistiller.banker.model.Transfer;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Handles all input
 */
public class Input {

    public enum MenuItem {
        EXIT("Exit"),
        LIST_CUSTOMERS("List customers"),
        CREATE_CUSTOMER("Create customer"),
        LIST_ACCOUNTS("List accounts of customer"),
        CREATE_ACCOUNT("Create account"),
        SHOW_BANK_STATEMENT("Show bank statement"),
        MAKE_TRANSFER("Make wire transfer");

        private String description;

        MenuItem(String description) {
            this.description = description;
        }
    }

    private Database db;
    private Scanner in;

    // Currencies are cached locally because they are used a lot
    // and are assumed to be static for the duration of an execution
    private List<String> currencies;

    Input(Database db) {
        this.db = db;
        in = new Scanner(System.in);
        currencies = db.getCurrencies();
    }

    MenuItem getMenuChoice() {
        int choice;
        boolean valid;

        do {
            System.out.println();
            System.out.println();
            System.out.println("MENU:");

            // Print menu in two columns
            for (int i = 1; i < MenuItem.values().length; i++) {
                System.out.printf("%d. %-28s", i, MenuItem.values()[i].description);
                if (i % 2 == 0) {
                    System.out.println();
                }
            }
            System.out.println("0. Exit");
            System.out.println();

            choice = getIntInput("Choice");

            valid = choice >= 0 && choice < MenuItem.values().length;;
            if (!valid) {
                System.out.println("Invalid menu choice");
            }
        } while(!valid);
        System.out.println();
        System.out.println();

        return MenuItem.values()[choice];
    }

    Customer getNewCustomer() {
        clearLine();

        var c = new Customer(getStringInputWithSpaces("Name"));
        c.setAddress(getStringInputWithSpaces("Address"));
        c.setEmail(getStringInput("Email"));
        c.setPhone(getStringInput("Phone"));

        System.out.println();

        return c;
    }

    int getCustomerId() {
        System.out.println();
        listCustomers();
        System.out.println();

        int id;
        boolean valid;

        do {
            id = getIntInput("Customer ID");

            valid = db.getCustomer(id) != null;
            if (!valid) {
                System.out.println("Customer not found");
            }
        } while (!valid);

        return id;
    }

    Account getNewAccount() {
        var ref = getNonExistingAccountRef();
        var initialBalance = getAmount("Initial balance");

        return new Account(ref.getCustomerId(), ref.getAccountNumber(), initialBalance);
    }

    Account.Reference getExistingAccountRef() {
        return getAccountRef(true);
    }

    Account.Reference getNonExistingAccountRef() {
        return getAccountRef(false);
    }

    private Account.Reference getAccountRef(boolean existingOrNew) {
        var customerId = getCustomerId();
        var customer = db.getCustomer(customerId);

        System.out.println();
        listAccounts(customer);
        System.out.println();

        int accountNo;
        Account.Reference ref;
        boolean valid = false;

        do {
            accountNo = getIntInput(existingOrNew ? "Account Number" : "New Account Number");
            ref = new Account.Reference(customerId, accountNo);

            if (existingOrNew) {
                if (db.getAccount(ref) != null) {
                    valid = true;
                } else {
                    System.out.println("Account not found");
                }
            } else {
                if (db.getAccount(ref) == null) {
                    valid = true;
                } else {
                    System.out.println("Account already exists");
                }
            }
        } while (!valid);

        return ref;
    }

    Transfer getNewTransfer() {
        System.out.println("Sender:");
        var sender = getExistingAccountRef();

        System.out.println();
        System.out.println("Receiver:");
        var receiver = getExistingAccountRef();

        var amount = getAmount();

        clearLine();
        var reference = getStringInputWithSpaces("Reference");

        return new Transfer(sender, receiver, amount, reference);
    }

    // Input helper methods including validation
    private int getIntInput(String prompt) {
        int input = 0;
        boolean valid = false;

        do {
            System.out.print(prompt + ": ");
            try {
                input = in.nextInt();
                valid = true;
            } catch (InputMismatchException ignore) {}

            if (!valid) {
                System.out.println("Invalid input");
                clearLine();
            }
        } while (!valid);

        return input;
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt + ": ");
        return in.next();
    }

    private String getStringInputWithSpaces(String prompt) {
        System.out.print(prompt + ": ");
        return in.nextLine();
    }

    private Amount getAmount() {
        return getAmount("Amount");
    }

    private Amount getAmount(String name) {
        double amount = 0;
        String currency = "";
        boolean validAmount = false;
        boolean validCurrency = false;

        do {
            System.out.print(name + " in " + getCurrencyList() + ": ");

            try {
                amount = in.nextDouble();
                currency = in.next();
                validAmount = true;
                validCurrency = true;
            } catch (InputMismatchException ignore) {}

            validAmount = validAmount && amount > 0;
            if (!validAmount) {
                System.out.println("Invalid amount, must be positive number");
            } else {
                validCurrency = validCurrency && currencies.contains(currency);
                if (!validCurrency) {
                    System.out.println("Invalid currency");
                }
            }

            if (!(validAmount && validCurrency)) {
                clearLine();
            }
        } while (!(validAmount && validCurrency));

        return new Amount(amount, currency);
    }

    // Other helper methods
    private void listCustomers() {
        System.out.println("CUSTOMERS:");
        System.out.println("ID   NAME");
        for (Customer c : db.getCustomers()) {
            System.out.printf("%-2d   %s\n", c.getId(), c.getName());
        }
    }

    private void listAccounts(Customer customer) {
        System.out.println("EXISTING ACCOUNTS FOR CUSTOMER " + customer.getName());
        System.out.println("NO.     BALANCE");
        for (Account a : db.getAccounts(customer.getId())) {
            System.out.printf("%-3d     %s\n",
                    a.getRef().getAccountNumber(),
                    db.getAccountBalance(a.getRef())
            );
        }
    }

    private String getCurrencyList() {
        return String.join("/", currencies);
    }

    void clearLine() {
        in.nextLine();
    }
}
