package civicloop.data;

import civicloop.model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataStore implements Serializable {

    private HashMap<String, User> users;
    private ArrayList<Item> items;
    private ArrayList<Service> services;
    private ArrayList<TimeCreditTransaction> transactions;
    private ArrayList<CommunityPost> posts;
    private HashMap<String, TrustScoreManager> trustManagers;
    // NEW: track which user borrowed which item (itemId -> borrowerUserId)
    private HashMap<String, String> itemBorrowerMap;

    // Constants for trust adjustments
    public static final int TRUST_INCREASE_BORROW = 5;
    public static final int TRUST_INCREASE_SERVICE = 5;
    public static final int TRUST_DECREASE_LATE_RETURN = 3;
    public static final int TRUST_DECREASE_FAKE_REQUEST = 10;
    public static final int TRUST_INCREASE_RETURN = 2;  // extra for returning

    public DataStore() {
        users = new HashMap<>();
        items = new ArrayList<>();
        services = new ArrayList<>();
        transactions = new ArrayList<>();
        posts = new ArrayList<>();
        trustManagers = new HashMap<>();
        itemBorrowerMap = new HashMap<>();   // initialize
    }

    // --- Registration ---
    public String registerUser(String name, String area, String password) {
        String newId = generateUserId();
        if (newId == null) {
            return null; // no free ID
        }
        User u = new User(name, area, password, newId);
        users.put(u.getUserId(), u);
        trustManagers.put(u.getUserId(), new TrustScoreManager(u.getUserId()));
        return u.getUserId();
    }

    private String generateUserId() {
        for (int id = 1000; id <= 5000; id++) {
            String idStr = String.valueOf(id);
            if (!users.containsKey(idStr)) {
                return idStr;
            }
        }
        return null; // fully booked
    }

    // --- Login ---
    public User login(String userId, String password) {
        User u = users.get(userId);
        if (u != null && u.checkPassword(password)) {
            return u;
        }
        return null; // generic failure
    }

    // --- User lookup ---
    public User findUser(String userId) { return users.get(userId); }
    public HashMap<String, User> getAllUsers() { return users; }
    public int getTrustScore(String userId) {
        TrustScoreManager tm = trustManagers.get(userId);
        return tm != null ? tm.getScore() : 0;
    }

    // --- Items ---
    public void addItem(String itemName, User owner) {
        items.add(new Item(itemName, owner.getUserId()));
    }

    public ArrayList<Item> getItems() { return items; }

    public String requestItem(String itemId, User borrower, double hours) {
        Item item = findItemById(itemId);
        if (item == null) return "Item not found.";
        if (!item.isAvailable()) return "Item is already borrowed.";
        if (item.getOwnerId().equals(borrower.getUserId()))
            return "You cannot borrow your own item.";

        User owner = findUser(item.getOwnerId());
        if (owner == null) return "Owner not found.";

        // Check if borrower has enough TC
        if (borrower.getTimeCreditBalance() < hours * item.getCreditRate()) {
            return "Insufficient TimeCredits. You need " + (hours * item.getCreditRate()) + " TC.";
        }

        TimeCreditTransaction t = new TimeCreditTransaction(
                borrower.getUserId(), owner.getUserId(), hours, item);
        double credit = t.getCreditAmount();
        borrower.setTimeCreditBalance(borrower.getTimeCreditBalance() - credit);
        owner.setTimeCreditBalance(owner.getTimeCreditBalance() + credit);

        // Trust: both gain
        trustManagers.get(borrower.getUserId()).increaseScore(TRUST_INCREASE_BORROW);
        trustManagers.get(owner.getUserId()).increaseScore(TRUST_INCREASE_BORROW);
        borrower.setTrustScore(trustManagers.get(borrower.getUserId()).getScore());
        owner.setTrustScore(trustManagers.get(owner.getUserId()).getScore());

        item.markBorrowed();
        // track borrower
        itemBorrowerMap.put(itemId, borrower.getUserId());
        transactions.add(t);
        return "Item borrowed successfully!";
    }

    // NEW: Return an item (borrower returns it)
    public String returnItem(String itemId, User returner) {
        Item item = findItemById(itemId);
        if (item == null) return "Item not found.";
        if (item.isAvailable()) return "Item is not currently borrowed.";

        String borrowerId = itemBorrowerMap.get(itemId);
        if (borrowerId == null) return "Borrower information missing.";

        // Only the borrower or the owner can return? For safety, allow both.
        if (!returner.getUserId().equals(borrowerId) && !returner.getUserId().equals(item.getOwnerId())) {
            return "You are not authorized to return this item.";
        }

        // Mark available, clear borrower
        item.markReturned();
        itemBorrowerMap.remove(itemId);

        // Optional: give trust boost to borrower for returning
        if (returner.getUserId().equals(borrowerId)) {
            trustManagers.get(borrowerId).increaseScore(TRUST_INCREASE_RETURN);
            User borrower = findUser(borrowerId);
            if (borrower != null) {
                borrower.setTrustScore(trustManagers.get(borrowerId).getScore());
            }
        }
        return "Item returned successfully.";
    }

    private Item findItemById(String id) {
        for (Item i : items) if (i.getItemId().equals(id)) return i;
        return null;
    }

    // --- Services ---
    public void addService(String serviceType, User provider) {
        services.add(new Service(serviceType, provider.getUserId()));
    }

    public ArrayList<Service> getServices() { return services; }

    public String requestService(String serviceId, User seeker, double hours) {
        Service s = findServiceById(serviceId);
        if (s == null) return "Service not found.";
        if (!s.isAvailable()) return "Service is currently busy.";
        if (s.getProviderId().equals(seeker.getUserId()))
            return "You cannot request your own service.";

        User provider = findUser(s.getProviderId());
        if (provider == null) return "Provider not found.";

        // Check balance
        double credit = hours * s.getCreditRate();
        if (seeker.getTimeCreditBalance() < credit) {
            return "Insufficient TimeCredits. You need " + credit + " TC.";
        }

        TimeCreditTransaction t = new TimeCreditTransaction(
                seeker.getUserId(), provider.getUserId(), hours, s);
        seeker.setTimeCreditBalance(seeker.getTimeCreditBalance() - credit);
        provider.setTimeCreditBalance(provider.getTimeCreditBalance() + credit);

        trustManagers.get(seeker.getUserId()).increaseScore(TRUST_INCREASE_SERVICE);
        trustManagers.get(provider.getUserId()).increaseScore(TRUST_INCREASE_SERVICE);
        seeker.setTrustScore(trustManagers.get(seeker.getUserId()).getScore());
        provider.setTrustScore(trustManagers.get(provider.getUserId()).getScore());

        s.markBusy();
        transactions.add(t);
        return "Service requested successfully!";
    }

    // NEW: Complete a service (provider marks it available)
    public String completeService(String serviceId, User provider) {
        Service s = findServiceById(serviceId);
        if (s == null) return "Service not found.";
        if (s.isAvailable()) return "Service is already available.";
        if (!s.getProviderId().equals(provider.getUserId())) {
            return "You are not the provider of this service.";
        }
        s.markAvailable();
        return "Service marked as complete and available.";
    }

    private Service findServiceById(String id) {
        for (Service s : services) if (s.getServiceId().equals(id)) return s;
        return null;
    }

    // --- Trust reports ---
    public void reportLateReturn(String userId) {
        TrustScoreManager tm = trustManagers.get(userId);
        if (tm != null) {
            tm.decreaseScore(TRUST_DECREASE_LATE_RETURN);
            findUser(userId).setTrustScore(tm.getScore());
        }
    }

    public void reportFakeRequest(String userId) {
        TrustScoreManager tm = trustManagers.get(userId);
        if (tm != null) {
            tm.decreaseScore(TRUST_DECREASE_FAKE_REQUEST);
            findUser(userId).setTrustScore(tm.getScore());
        }
    }

    // --- Community Feed ---
    public void addPost(String authorId, String content) {
        posts.add(CommunityPost.createPost(authorId, content));
    }

    public ArrayList<CommunityPost> getPosts() { return posts; }

    public void likePost(String postId) {
        for (CommunityPost p : posts) {
            if (p.getPostId().equals(postId)) {
                p.addLike();
                break;
            }
        }
    }

    // NEW: Add comment to a post
    public void addCommentToPost(String postId, String commenterId, String comment) {
        for (CommunityPost p : posts) {
            if (p.getPostId().equals(postId)) {
                p.addComment(commenterId + ": " + comment);
                break;
            }
        }
    }

    // --- Transactions ---
    public ArrayList<TimeCreditTransaction> getTransactions() { return transactions; }

    // NEW: Get items borrowed by a user
    public ArrayList<Item> getItemsBorrowedByUser(String userId) {
        ArrayList<Item> borrowed = new ArrayList<>();
        for (Map.Entry<String, String> entry : itemBorrowerMap.entrySet()) {
            if (entry.getValue().equals(userId)) {
                Item item = findItemById(entry.getKey());
                if (item != null) borrowed.add(item);
            }
        }
        return borrowed;
    }

    // NEW: Get services provided by a user that are busy
    public ArrayList<Service> getBusyServicesByProvider(String providerId) {
        ArrayList<Service> busy = new ArrayList<>();
        for (Service s : services) {
            if (s.getProviderId().equals(providerId) && !s.isAvailable()) {
                busy.add(s);
            }
        }
        return busy;
    }

    // --- Persistence ---
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    public static DataStore loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (DataStore) ois.readObject();
        }
    }
}