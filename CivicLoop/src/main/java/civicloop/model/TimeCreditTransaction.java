package civicloop.model;

import java.io.Serializable;

public class TimeCreditTransaction implements Serializable {
    private String transactionId;
    private String fromUserId;
    private String toUserId;
    private double hoursSpent;
    private double creditAmount;
    private String type;

    public TimeCreditTransaction(String transactionId, String fromUserId, String toUserId,
                                 double hoursSpent, Creditable offer) {
        this.transactionId = transactionId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.hoursSpent = hoursSpent;
        this.creditAmount = hoursSpent * offer.getCreditRate();
        this.type = offer.getOfferType();
    }

    // Getters (unchanged)
    public String getTransactionId() { return transactionId; }
    public String getFromUserId() { return fromUserId; }
    public String getToUserId() { return toUserId; }
    public double getHoursSpent() { return hoursSpent; }
    public double getCreditAmount() { return creditAmount; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return String.format("%s -> %s : %.1f hrs (%s) = %.1f TC",
                fromUserId, toUserId, hoursSpent, type, creditAmount);
    }
}