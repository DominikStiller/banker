package de.domistiller.banker;

import de.domistiller.banker.model.Account;
import de.domistiller.banker.model.Customer;

import java.util.Scanner;

/**
 * Handles all input
 */
public class Input {

    public enum MenuItem {
        EXIT("Exit"),
        LIST_CUSTOMERS("List customers"),
        CREATE_CUSTOMER("Create customer"),
        DELETE_CUSTOMER("Delete customer"),
        LIST_ACCOUNTS("List accounts of customer"),
        CREATE_ACCOUNT("Create account"),
        DELETE_ACCOUNT("Delete account"),
        SHOW_BANK_STATEMENT("Show bank statement"),
        MAKE_TRANSFER("Make wire transfer");

        private String description;

        MenuItem(String description) {
            this.description = description;
        }
    }

    private Database db;
    private Scanner in;

    Input(Database db) {
        this.db = db;
        in = new Scanner(System.in);
    }

    MenuItem getMenuChoice() {
        int choice;

        do {
            System.out.println();
            System.out.println();
            System.out.println("MENU:");

            for (int i = 1; i < MenuItem.values().length; i++) {
                System.out.printf("%d. %-28s", i, MenuItem.values()[i].description);
                if (i % 3 == 0) {
                    System.out.println();
                }
            }
            System.out.println("0. Exit");
            System.out.println();
            System.out.print("Choice: ");

            choice = in.nextInt();
            if (!validateMenuChoice(choice)) {
                System.out.println("Invalid menu choice");
            }
        } while(!validateMenuChoice(choice));
        System.out.println();
        System.out.println();

        return MenuItem.values()[choice];
    }

    private boolean validateMenuChoice(int choice) {
        return choice >= 0 && choice < MenuItem.values().length;
    }

    Customer getNewCustomer() {
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
        return in.nextInt();
    }

    Account getNewAccount() {
        var ref = getAccountRef();

        System.out.print("Type (checking/savings): ");
        var type = Account.Type.fromString(in.next());

        System.out.print("Currency (CAD/USD/EUR): ");
        var currency = in.next();

        System.out.print("Initial Balance in " + currency + ": ");
        var initialBalance = in.nextDouble();

        return new Account(ref.getCustomerId(), ref.getAccountNumber(), type, currency, initialBalance);
    }

    Account.Reference getAccountRef() {
        var customerId = getCustomerId();

        var customer = db.getCustomer(customerId);
        if (customer == null) {
            System.out.println("Customer not found");
            return null;
        }
        System.out.println();

        listAccounts(customer);

        System.out.println();
        System.out.print("Account Number: ");
        var accountNo = in.nextInt();

        return new Account.Reference(customerId, accountNo);
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
        System.out.println("NO.   TYPE        CURRENCY");
        for (Account a : db.getAccounts(customer.getId())) {
            System.out.printf("%-3d   %-9s   %s\n",
                    a.getRef().getAccountNumber(),
                    a.getType(),
                    a.getInitialBalance().getCurrency()
            );
        }
    }
}
