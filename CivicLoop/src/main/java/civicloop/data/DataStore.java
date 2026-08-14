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
    private HashMap<String, String> itemBorrowerMap;

    // Counters for numeric IDs
    private int nextItemId = 1;
    private int nextServiceId = 1;
    private int nextTransactionId = 1;
    private int nextPostId = 1;

    public static final int TRUST_INCREASE_BORROW = 5;
    public static final int TRUST_INCREASE_SERVICE = 5;
    public static final int TRUST_DECREASE_LATE_RETURN = 3;   // unused now, kept for compatibility
    public static final int TRUST_DECREASE_FAKE_REQUEST = 10; // unused now, kept for compatibility
    public static final int TRUST_DECREASE_REPORT = 10;       // NEW: single fixed report penalty
    public static final int TRUST_INCREASE_RETURN = 2;
    public static final double INITIAL_TC = 10.0;

    public DataStore() {
        users = new HashMap<>();
        items = new ArrayList<>();
        services = new ArrayList<>();
        transactions = new ArrayList<>();
        posts = new ArrayList<>();
        trustManagers = new HashMap<>();
        itemBorrowerMap = new HashMap<>();
    }

    // ---- ID generators ----
    public String getNextItemId() { return String.valueOf(nextItemId++); }
    public String getNextServiceId() { return String.valueOf(nextServiceId++); }
    public String getNextTransactionId() { return String.valueOf(nextTransactionId++); }
    public String getNextPostId() { return String.valueOf(nextPostId++); }

    // ---- Registration ----
    public String registerUser(String name, String area, String password) {
        String newId = generateUserId();
        if (newId == null) return null;
        User u = new User(name, area, password, newId);
        u.setTimeCreditBalance(INITIAL_TC); // give initial credits
        users.put(u.getUserId(), u);
        trustManagers.put(u.getUserId(), new TrustScoreManager(u.getUserId()));
        return u.getUserId();
    }

    private String generateUserId() {
        for (int id = 1000; id <= 5000; id++) {
            String idStr = String.valueOf(id);
            if (!users.containsKey(idStr)) return idStr;
        }
        return null;
    }

    // ---- Login ----
    public User login(String userId, String password) {
        User u = users.get(userId);
        if (u != null && u.checkPassword(password)) return u;
        return null;
    }

    // ---- Getters ----
    public User findUser(String userId) { return users.get(userId); }
    public HashMap<String, User> getAllUsers() { return users; }
    public int getTrustScore(String userId) {
        TrustScoreManager tm = trustManagers.get(userId);
        return tm != null ? tm.getScore() : 0;
    }

    // ---- Items ----
    public void addItem(String itemName, User owner) {
        Item item = new Item(getNextItemId(), itemName, owner.getUserId());
        items.add(item);
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

        double credit = hours * item.getCreditRate();
        if (borrower.getTimeCreditBalance() < credit)
            return "Insufficient TimeCredits. You need " + credit + " TC.";

        TimeCreditTransaction t = new TimeCreditTransaction(
                getNextTransactionId(),
                borrower.getUserId(), owner.getUserId(),
                hours, item);
        borrower.setTimeCreditBalance(borrower.getTimeCreditBalance() - credit);
        owner.setTimeCreditBalance(owner.getTimeCreditBalance() + credit);

        trustManagers.get(borrower.getUserId()).increaseScore(TRUST_INCREASE_BORROW);
        trustManagers.get(owner.getUserId()).increaseScore(TRUST_INCREASE_BORROW);
        borrower.setTrustScore(trustManagers.get(borrower.getUserId()).getScore());
        owner.setTrustScore(trustManagers.get(owner.getUserId()).getScore());

        item.markBorrowed();
        itemBorrowerMap.put(itemId, borrower.getUserId());
        transactions.add(t);
        return "Item borrowed successfully!";
    }

    public String returnItem(String itemId, User returner) {
        Item item = findItemById(itemId);
        if (item == null) return "Item not found.";
        if (item.isAvailable()) return "Item is not currently borrowed.";

        String borrowerId = itemBorrowerMap.get(itemId);
        if (borrowerId == null) return "Borrower information missing.";
        if (!returner.getUserId().equals(borrowerId) && !returner.getUserId().equals(item.getOwnerId()))
            return "You are not authorized to return this item.";

        item.markReturned();
        itemBorrowerMap.remove(itemId);

        if (returner.getUserId().equals(borrowerId)) {
            trustManagers.get(borrowerId).increaseScore(TRUST_INCREASE_RETURN);
            User borrower = findUser(borrowerId);
            if (borrower != null) borrower.setTrustScore(trustManagers.get(borrowerId).getScore());
        }
        return "Item returned successfully.";
    }

    private Item findItemById(String id) {
        for (Item i : items) if (i.getItemId().equals(id)) return i;
        return null;
    }

    // ---- Services ----
    public void addService(String serviceType, User provider) {
        Service s = new Service(getNextServiceId(), serviceType, provider.getUserId());
        services.add(s);
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

        double credit = hours * s.getCreditRate();
        if (seeker.getTimeCreditBalance() < credit)
            return "Insufficient TimeCredits. You need " + credit + " TC.";

        TimeCreditTransaction t = new TimeCreditTransaction(
                getNextTransactionId(),
                seeker.getUserId(), provider.getUserId(),
                hours, s);
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

    public String completeService(String serviceId, User provider) {
        Service s = findServiceById(serviceId);
        if (s == null) return "Service not found.";
        if (s.isAvailable()) return "Service is already available.";
        if (!s.getProviderId().equals(provider.getUserId()))
            return "You are not the provider of this service.";
        s.markAvailable();
        return "Service marked as complete and available.";
    }

    private Service findServiceById(String id) {
        for (Service s : services) if (s.getServiceId().equals(id)) return s;
        return null;
    }

    // ---- Trust report (single type, tied to ONE transaction) ----
    /**
     * Reports the OTHER party of a transaction. Only the person who
     * offered the item/service (transaction's toUserId — the owner or
     * provider) is allowed to file the report, against the borrower/
     * seeker (fromUserId). Fixed score penalty. A transaction can only
     * ever be reported once.
     */
    public String reportTransaction(String transactionId, String reporterId) {
        TimeCreditTransaction t = findTransactionById(transactionId);
        if (t == null) return "Transaction not found.";
        if (t.isReported()) return "This transaction has already been reported.";
        if (!t.getToUserId().equals(reporterId))
            return "Only the person who offered the item/service can report this transaction.";

        String targetUserId = t.getFromUserId();
        TrustScoreManager tm = trustManagers.get(targetUserId);
        if (tm != null) {
            tm.decreaseScore(TRUST_DECREASE_REPORT);
            User u = findUser(targetUserId);
            if (u != null) u.setTrustScore(tm.getScore());
        }
        t.markReported();
        return "Report submitted.";
    }

    private TimeCreditTransaction findTransactionById(String id) {
        for (TimeCreditTransaction t : transactions) {
            if (t.getTransactionId().equals(id)) return t;
        }
        return null;
    }

    // ---- Community Feed ----
    public void addPost(String authorId, String content) {
        CommunityPost p = CommunityPost.createPost(getNextPostId(), authorId, content);
        posts.add(p);
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

    /**
     * Adds a comment to a post. Stored as "commenterId|timestamp|commentText"
     * so FeedPanel can show name/id + time on one line (post-card style).
     * The "|" delimiter is chosen because it won't normally appear in
     * plain comment text typed by users.
     */
    public void addCommentToPost(String postId, String commenterId, String comment) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
        for (CommunityPost p : posts) {
            if (p.getPostId().equals(postId)) {
                p.addComment(commenterId + "|" + timestamp + "|" + comment);
                break;
            }
        }
    }

    // ---- Transactions ----
    public ArrayList<TimeCreditTransaction> getTransactions() { return transactions; }

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

    public ArrayList<Service> getBusyServicesByProvider(String providerId) {
        ArrayList<Service> busy = new ArrayList<>();
        for (Service s : services) {
            if (s.getProviderId().equals(providerId) && !s.isAvailable()) {
                busy.add(s);
            }
        }
        return busy;
    }

    /**
     * Returns true if there is at least one recorded transaction (item
     * borrow or service request) between the two given users, in either
     * direction. Kept for compatibility; no longer used by the report
     * flow (which is now transaction-based), but harmless to keep.
     */
    public boolean hasTransactionWith(String userIdA, String userIdB) {
        for (TimeCreditTransaction t : transactions) {
            boolean pair = (t.getFromUserId().equals(userIdA) && t.getToUserId().equals(userIdB))
                    || (t.getFromUserId().equals(userIdB) && t.getToUserId().equals(userIdA));
            if (pair) return true;
        }
        return false;
    }

    // ---- Persistence ----
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

    // ---- Reload from file (for multi-window sync) ----
    public void reloadFromFile(String filename) throws IOException, ClassNotFoundException {
        DataStore loaded = loadFromFile(filename);
        this.users.clear(); this.users.putAll(loaded.users);
        this.items.clear(); this.items.addAll(loaded.items);
        this.services.clear(); this.services.addAll(loaded.services);
        this.transactions.clear(); this.transactions.addAll(loaded.transactions);
        this.posts.clear(); this.posts.addAll(loaded.posts);
        this.trustManagers.clear(); this.trustManagers.putAll(loaded.trustManagers);
        this.itemBorrowerMap.clear(); this.itemBorrowerMap.putAll(loaded.itemBorrowerMap);
        this.nextItemId = loaded.nextItemId;
        this.nextServiceId = loaded.nextServiceId;
        this.nextTransactionId = loaded.nextTransactionId;
        this.nextPostId = loaded.nextPostId;
    }
}