package de.domistiller.banker.model;

import java.util.Date;

public class Transfer {

    private int id;
    private Account.Reference sender;
    private Account.Reference receiver;
    private Amount amount;
    private Date date;
    private String reference;

    public Transfer(int id, Account.Reference sender, Account.Reference receiver, Amount amount, Date date, String reference) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.date = date;
        this.reference = reference;
    }

    public int getId() {
        return id;
    }

    public Account.Reference getSender() {
        return sender;
    }

    public Account.Reference getReceiver() {
        return receiver;
    }

    public Amount getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getReference() {
        return reference;
    }

    public boolean isIncomingFor(Account.Reference receiver) {
        return this.receiver.equals(receiver);
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "id=" + id +
                ", sender=" + sender +
                ", receiver=" + receiver +
                ", amount=" + amount +
                ", date=" + date +
                ", reference='" + reference + '\'' +
                '}';
    }
}
