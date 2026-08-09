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





}





