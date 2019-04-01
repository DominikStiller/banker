package de.domistiller.banker.model;

import java.time.LocalDateTime;

public class Transfer {

    private int id;
    private Account.Reference sender;
    private String senderName;
    private Account.Reference receiver;
    private String receiverName;
    private Amount amount;
    private LocalDateTime executionDate;
    private String reference;

    public Transfer(Account.Reference sender, Account.Reference receiver, Amount amount, String reference) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.reference = reference;
        executionDate = LocalDateTime.now();
    }

    public Transfer(int id, Account.Reference sender, Account.Reference receiver, Amount amount, LocalDateTime executionDate, String reference) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.executionDate = executionDate;
        this.reference = reference;
    }

    public Transfer(int id, Account.Reference sender, String senderName, Account.Reference receiver, String receiverName,
                    Amount amount, LocalDateTime executionDate, String reference) {
        this(id, sender, receiver, amount, executionDate, reference);
        this.senderName = senderName;
        this.receiverName = receiverName;
    }

    public int getId() {
        return id;
    }

    public Account.Reference getSender() {
        return sender;
    }

    public void setSender(Account.Reference sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public Account.Reference getReceiver() {
        return receiver;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public Amount getAmount() {
        return amount;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public String getReference() {
        return reference;
    }

    public char getSignFor(Account.Reference account) {
        if (account.equals(this.sender)) {
            return '-';
        } else if (account.equals(this.receiver)) {
            return '+';
        } else {
            throw new IllegalArgumentException("Account must either be sender or receiver");
        }
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "id=" + id +
                ", sender=" + sender +
                ", receiver=" + receiver +
                ", amount=" + amount +
                ", executionDate=" + executionDate +
                ", reference='" + reference + '\'' +
                '}';
    }
}
