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









}












