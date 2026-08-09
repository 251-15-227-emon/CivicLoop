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






}





