package civicloop.model;

import java.io.Serializable;
import java.util.UUID;

public class Item implements Creditable, Serializable {
    private String itemId;
    private String itemName;
    private String ownerId;
    private boolean isAvailable;

    public Item(String itemName, String ownerId) {
        this.itemId = UUID.randomUUID().toString().substring(0, 8);
        this.itemName = itemName;
        this.ownerId = ownerId;
        this.isAvailable = true;
    }

    @Override
    public double getCreditRate() {
        return 0.5;
    }

    @Override
    public String getOfferType() {
        return "Item";
    }

    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getOwnerId() { return ownerId; }
    public boolean isAvailable() { return isAvailable; }
    public void markBorrowed() { this.isAvailable = false; }
    public void markReturned() { this.isAvailable = true; }
}