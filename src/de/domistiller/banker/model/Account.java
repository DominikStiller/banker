package de.domistiller.banker.model;

import java.util.Objects;

public class Account {

    private Reference ref;
    private Amount initialBalance;

    public Account(int customerId, int accountNumber, Amount initialBalance) {
        this.ref = new Reference(customerId, accountNumber);
        this.initialBalance = initialBalance;
    }

    public Reference getRef() {
        return ref;
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
}
