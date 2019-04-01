package de.domistiller.banker.model;

public class Amount {
    private double amount;
    private String currency;

    public Amount(double amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public char getSign() {
        return amount >= 0 ? '+' : '-';
    }

    public Amount toAbsolute() {
        return new Amount(Math.abs(amount), currency);
    }

    public Amount minus(Amount other) {
        if (!other.currency.equals(currency)) {
            throw new IllegalArgumentException("Only amounts of the same currency can be used");
        }
        return new Amount(this.amount - other.amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency);
    }
}
