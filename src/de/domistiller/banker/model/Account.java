package de.domistiller.banker.model;

import java.util.Objects;

public class Account {

    private Reference ref;
    private Type type;
    private Amount initialBalance;

    public Account(int customerId, int accountNumber, Type type, Amount initialBalance) {
        this.ref = new Reference(customerId, accountNumber);
        this.type = type;
        this.initialBalance = initialBalance;
    }

    public Reference getRef() {
        return ref;
    }

    public Type getType() {
        return type;
    }

    public Amount getInitialBalance() {
        return initialBalance;
    }


    /**
     * Represents a primary key for an account
     * Consists of the customer id and the account number
     */
    public static class Reference {
        private int customerId;
        private int accountNumber;

        public Reference(int customerId, int accountNumber) {
            this.customerId = customerId;
            this.accountNumber = accountNumber;
        }

        public int getCustomerId() {
            return customerId;
        }

        public int getAccountNumber() {
            return accountNumber;
        }

        @Override
        public String toString() {
            return customerId + "-" + accountNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Reference reference = (Reference) o;
            return customerId == reference.customerId &&
                    accountNumber == reference.accountNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hash(customerId, accountNumber);
        }
    }


    public enum Type {

        SAVINGS,
        CHECKING;

        public static Type fromString(String type) {
            switch (type) {
                case "savings":
                    return SAVINGS;
                case "checking":
                    return CHECKING;
                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            switch(this) {
                case SAVINGS:
                    return "Savings";
                case CHECKING:
                    return "Checking";
                default:
                    return "Unknown";
            }
        }
    }
}
