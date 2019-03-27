package de.domistiller.banker;

import de.domistiller.banker.model.Account;
import de.domistiller.banker.model.Amount;
import de.domistiller.banker.model.Customer;
import de.domistiller.banker.model.Transfer;

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
            System.out.print("Choice: ");

            choice = in.nextInt();

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
        System.out.print("Name: ");
        var c = new Customer(in.nextLine());

        System.out.print("Address: ");
        c.setAddress(in.nextLine());

        System.out.print("Email: ");
        c.setEmail(in.next());

        System.out.print("Phone: ");
        c.setPhone(in.next());

        System.out.println();

        return c;
    }

    int getCustomerId() {
        System.out.println();
        listCustomers();
        System.out.println();

        System.out.print("Customer ID: ");
        var id = in.nextInt();
        var c = db.getCustomer(id);
        if (c == null) {
            System.out.println("Customer not found");
            return 0;
        }

        return id;
    }

    Account getNewAccount() {
        var ref = getAccountRef();
        if (ref == null) {
            return null;
        }

        System.out.print("Type (checking/savings): ");
        var type = Account.Type.fromString(in.next());

        var initialBalance = getAmount("Initial balance");

        return new Account(ref.getCustomerId(), ref.getAccountNumber(), type, initialBalance);
    }

    Account.Reference getAccountRef() {
        var customerId = getCustomerId();
        if (customerId == 0) {
            return null;
        }

        var customer = db.getCustomer(customerId);
        System.out.println();

        listAccounts(customer);

        System.out.println();
        System.out.print("Account Number: ");
        var accountNo = in.nextInt();

        return new Account.Reference(customerId, accountNo);
    }

    Transfer getNewTransfer() {
        System.out.println("Sender:");
        var sender = getAccountRef();
        if (sender == null) {
            return null;
        }
        if (db.getAccount(sender) == null) {
            System.out.println("Account not found");
            return null;
        }

        System.out.println();
        System.out.println("Receiver:");
        var receiver = getAccountRef();
        if (receiver == null) {
            return null;
        }
        if (db.getAccount(receiver) == null) {
            System.out.println("Account not found");
            return null;
        }

        var amount = getAmount();

        clearLine();
        System.out.print("Reference: ");
        var reference = in.nextLine();

        return new Transfer(sender, receiver, amount, reference);
    }

    /*
     * Helper methods
     */
    private void listCustomers() {
        System.out.println("CUSTOMERS:");
        System.out.println("ID   NAME");
        for (Customer c : db.getCustomers()) {
            System.out.printf("%-2d   %s\n", c.getId(), c.getName());
        }
    }

    private void listAccounts(Customer customer) {
        System.out.println("ACCOUNTS FOR CUSTOMER " + customer.getName());
        System.out.println("NO.   TYPE        BALANCE");
        for (Account a : db.getAccounts(customer.getId())) {
            System.out.printf("%-3d   %-9s   %s\n",
                    a.getRef().getAccountNumber(),
                    a.getType(),
                    db.getAccountBalance(a.getRef())
            );
        }
    }

    private Amount getAmount() {
        return getAmount("Amount");
    }

    private Amount getAmount(String name) {
        double amount;
        String currency;
        boolean valid;

        do {
            System.out.print(name + " in " + getCurrencyList() + ": ");
            amount = in.nextDouble();
            currency = in.next();

            valid = currencies.contains(currency);
            if (!valid) {
                System.out.println("Invalid currency");
            }
        } while (!valid);

        return new Amount(amount, currency);
    }

    private String getCurrencyList() {
        return String.join("/", currencies);
    }

    void clearLine() {
        in.nextLine();
    }
}
