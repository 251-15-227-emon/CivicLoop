package civicloop.model;

import java.io.Serializable;
import java.util.UUID;

public class TimeCreditTransaction implements Serializable {
    private String transactionId;
    private String fromUserId;   // the spender
    private String toUserId;     // the earner
    private double hoursSpent;
    private double creditAmount;
    private String type;         // "Item" or "Service"
    



     public TimeCreditTransaction(String fromUserId, String toUserId,
        double hoursSpent, Creditable offer) {
        this.transactionId = UUID.randomUUID().toString().substring(0, 8);
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.hoursSpent = hoursSpent;
        this.creditAmount = calculateCredit(hoursSpent, offer);
        this.type = offer.getOfferType();
    }

    /**
     * Polymorphic call: the same method behaves differently depending on
     * whether 'offer' is an Item (rate 0.5) or a Service (rate 1.0).
     */
    private double calculateCredit(double hours, Creditable offer) {
        return hours * offer.getCreditRate();
    }


    // Getters
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

// DONE 















