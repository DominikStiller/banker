package de.domistiller.banker;

import de.domistiller.banker.model.Customer;

import java.util.Scanner;

public class Input {

    public enum MenuItem {
        EXIT,
        LIST_CUSTOMERS,
        CREATE_CUSTOMER,
        DELETE_CUSTOMER
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

    Customer getCustomer() {
        System.out.print("Name: ");
        var c = new Customer(in.next());

        System.out.print("Address: ");
        c.setAddress(in.next());

        System.out.print("Email: ");
        c.setEmail(in.next());

        System.out.print("Phone: ");
        c.setPhone(in.next());

        System.out.println();

        return c;
    }

    int getCustomerId() {
        System.out.print("Customer ID: ");
        var id = in.nextInt();
        System.out.println();
        return id;
    }
}
