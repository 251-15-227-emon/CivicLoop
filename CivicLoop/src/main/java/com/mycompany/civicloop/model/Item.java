package civicloop.model;

import java.io.Serializable;
import java.util.UUID;/**
 * Represents a physical item that can be shared.
 * ENCAPSULATION: all fields private, getters provide controlled access.
 */



public class Item implements Creditable, Serializable {

 private String itemId;
    private String itemName;
    private String ownerId;
    private boolean isAvailable;   // true = can be borrowed



 public Item(String itemName, String ownerId) {
        this.itemId = UUID.randomUUID().toString().substring(0, 8);
        this.itemName = itemName;
        this.ownerId = ownerId;
        this.isAvailable = true;
    }











}















