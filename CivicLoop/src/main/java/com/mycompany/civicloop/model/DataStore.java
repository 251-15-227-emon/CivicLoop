package civicloop.data;

import civicloop.model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * SINGLE SOURCE OF TRUTH for all data.
 * Handles storage, business logic (exchange, trust updates), and file persistence.
 */


public class DataStore implements Serializable {


 private HashMap<String, User> users;
    private ArrayList<Item> items;
    private ArrayList<Service> services;
    private ArrayList<TimeCreditTransaction> transactions;
    private ArrayList<CommunityPost> posts;
    private HashMap<String, TrustScoreManager> trustManagers; // userId -> manager

    public DataStore() {
        users = new HashMap<>();
        items = new ArrayList<>();
        services = new ArrayList<>();
        transactions = new ArrayList<>();
        posts = new ArrayList<>();
        trustManagers = new HashMap<>();
    }

     // ---- User Account (Member 1) ----
    public String registerUser(String name, String area, String password) {
        User u = new User(name, area, password);
        users.put(u.getUserId(), u);
        trustManagers.put(u.getUserId(), new TrustScoreManager(u.getUserId()));
        return u.getUserId();
    }

    public User login(String userId, String password) {
        User u = users.get(userId);
        if (u != null && u.checkPassword(password)) return u;
        return null; // login failed
    }

    public User findUser(String userId) { return users.get(userId); }
    public HashMap<String, User> getAllUsers() { return users; }

    // ---- Item Sharing (Member 2) ----
    public void addItem(String itemName, User owner) {
        items.add(new Item(itemName, owner.getUserId()));
    }
    


    
    public ArrayList<Item> getItems() { return items; }

    /**
     * Core exchange logic for item borrowing (follows activity diagram).
     * Borrower spends TimeCredits, owner earns them. Trust scores increase.
     */
    public String requestItem(String itemId, User borrower, double hours) {
        Item item = findItemById(itemId);
        if (item == null) return "Item not found.";
        if (!item.isAvailable()) return "Item is already borrowed.";
        if (item.getOwnerId().equals(borrower.getUserId()))
            return "You cannot borrow your own item.";

        User owner = findUser(item.getOwnerId());
        if (owner == null) return "Owner not found.";

        // Create transaction (polymorphism: Item implements Creditable)
        TimeCreditTransaction t = new TimeCreditTransaction(
                borrower.getUserId(), owner.getUserId(), hours, item);

        // Update balances
        double credit = t.getCreditAmount();
        borrower.setTimeCreditBalance(borrower.getTimeCreditBalance() - credit);
        owner.setTimeCreditBalance(owner.getTimeCreditBalance() + credit);

        
        // Update trust scores (both gain trust)
        trustManagers.get(borrower.getUserId()).increaseScore(5);
        trustManagers.get(owner.getUserId()).increaseScore(5);
        // Sync user objects
        borrower.setTrustScore(trustManagers.get(borrower.getUserId()).getScore());
        owner.setTrustScore(trustManagers.get(owner.getUserId()).getScore());

        item.markBorrowed();
        transactions.add(t);
        return "Item borrowed successfully!";
    }






}





