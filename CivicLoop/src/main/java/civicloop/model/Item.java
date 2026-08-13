package civicloop.model;

import java.io.Serializable;

public class Item implements Creditable, Serializable {
    private String itemId;
    private String itemName;
    private String ownerId;
    private boolean isAvailable;

    public Item(String itemId, String itemName, String ownerId) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.ownerId = ownerId;
        this.isAvailable = true;
    }

    @Override
    public double getCreditRate() { return 0.5; }
    @Override
    public String getOfferType() { return "Item"; }

    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getOwnerId() { return ownerId; }
    public boolean isAvailable() { return isAvailable; }
    public void markBorrowed() { this.isAvailable = false; }
    public void markReturned() { this.isAvailable = true; }
}