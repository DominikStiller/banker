package de.domistiller.banker;

import de.domistiller.banker.model.Account;
import de.domistiller.banker.model.Amount;
import de.domistiller.banker.model.Customer;
import de.domistiller.banker.model.Transfer;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Handles all input
 */
public class Input {

    public enum MenuChoice {
        EXIT("Exit"),
        LIST_CUSTOMERS("List customers"),
        LIST_ACCOUNTS("List accounts of customer"),
        CREATE_CUSTOMER("Create customer"),
        CREATE_ACCOUNT("Create account"),
        SHOW_BANK_STATEMENT("Show bank statement"),
        MAKE_TRANSFER("Make wire transfer");

        private String description;

        MenuChoice(String description) {
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

    MenuChoice getMenuChoice() {
        int choice;
        boolean valid;

        do {
            System.out.println();
            System.out.println();
            System.out.println("MENU:");

            // Print all menu choices in two columns
            for (int i = 1; i < MenuChoice.values().length; i++) {
                System.out.printf("%d. %-28s", i, MenuChoice.values()[i].description);
                if (i % 2 == 0) {
                    System.out.println();
                }
            }
            System.out.println("0. Exit");
            System.out.println();

            choice = getIntInput("Choice");

            valid = choice >= 0 && choice < MenuChoice.values().length;;
            if (!valid) {
                System.out.println("Invalid menu choice");
            }
        } while(!valid);

        System.out.println();
        System.out.println();

        return MenuChoice.values()[choice];
    }


    // CUSTOMERS
    Customer getNewCustomer() {
        clearLine();

        var customer = new Customer(getStringInputWithSpaces("Name"));
        customer.setAddress(getStringInputWithSpaces("Address"));
        customer.setEmail(getStringInput("Email"));
        customer.setPhone(getStringInput("Phone"));

        System.out.println();

        return customer;
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


    // ACCOUNTS
    Account getNewAccount() {
        var accountRef = getNonExistingAccountRef();
        var initialBalance = getAmount("Initial balance");

        return new Account(accountRef.getCustomerId(), accountRef.getAccountNumber(), initialBalance);
    }

    Account.Reference getExistingAccountRef() {
        return getAccountRef((ref) -> {
            if (db.getAccount(ref) == null) {
                System.out.println("Account not found");
                return false;
            }
            return true;
        });
    }

    Account.Reference getNonExistingAccountRef() {
        return getAccountRef((ref) -> {
            if (db.getAccount(ref) != null) {
                System.out.println("Account already exists");
                return false;
            }
            return true;
        });
    }

    private Account.Reference getAccountRef(Predicate<Account.Reference> validator) {
        // Account reference concists of customer id and account number
        var customerId = getCustomerId();
        var customer = db.getCustomer(customerId);

        System.out.println();
        listAccounts(customer);
        System.out.println();

        Account.Reference accountRef;
        boolean valid;

        do {
            accountRef = new Account.Reference(customerId, getIntInput("Account Number"));
            valid = validator.test(accountRef);
        } while (!valid);

        return accountRef;
    }


    // TRANSFERS
    Transfer getNewTransfer() {
        System.out.println("Sender:");
        var sender = getExistingAccountRef();

        System.out.println();
        System.out.println("Receiver:");
        var receiver = getExistingAccountRef();

        var amount = getAmount("Amount");

        clearLine();
        var reference = getStringInputWithSpaces("Reference");

        return new Transfer(sender, receiver, amount, reference);
    }


    // Input helper methods for various types, including validation
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

            // TODO Simplify validation
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
