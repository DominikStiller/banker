package de.domistiller.banker;

import de.domistiller.banker.model.Account;
import de.domistiller.banker.model.Customer;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Input {

    public enum MenuItem {
        EXIT,
        LIST_CUSTOMERS,
        CREATE_CUSTOMER,
        DELETE_CUSTOMER,
        LIST_ACCOUNTS,
        LIST_TRANSFERS
    }

    private Scanner in;

    Input() {
        in = new Scanner(System.in);
    }

    MenuItem getMenuChoice() {
        int choice;

        do {
            System.out.println();
            System.out.println();
            System.out.println("Menu Choices:");
            System.out.println("1. List customers");
            System.out.println("2. Create customer");
            System.out.println("3. Delete customer");
            System.out.println("4. List accounts");
            System.out.println("5. List transfers");
            System.out.println("0. Exit");
            System.out.println();
            System.out.print("Choice: ");

            choice = in.nextInt();
            if (!valdiateMenuChoice(choice)) {
                System.out.println("Invalid menu choice");
            }
        } while(!valdiateMenuChoice(choice));
        System.out.println();
        System.out.println();

        return MenuItem.values()[choice];
    }

    private boolean valdiateMenuChoice(int choice) {
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

    int getCustomerId(Runnable listCustomers) {
        System.out.println();
        listCustomers.run();
        System.out.println();
        System.out.print("Customer ID: ");
        var id = in.nextInt();
        System.out.println();
        return id;
    }

    Account.Reference getAccountRef(Runnable listCustomers, Predicate<Integer> listAccounts) {
        System.out.println();
        listCustomers.run();
        System.out.println();
        System.out.print("Customer ID: ");
        var customer = in.nextInt();

        System.out.println();
        var customerExists = listAccounts.test(customer);

        if (!customerExists) {
            System.out.println("Customer not found");
            return null;
        }

        System.out.println();
        System.out.print("Account Number: ");
        var accountNo = in.nextInt();
        System.out.println();

        return new Account.Reference(customer, accountNo);
    }
}
