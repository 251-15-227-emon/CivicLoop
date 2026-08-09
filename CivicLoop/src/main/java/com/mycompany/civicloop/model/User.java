package civicloop.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Represents a community member.
 * ENCAPSULATION: password is private – no public getter, only checkPassword().
 */

public class User implements Serializable {
    private String userId;
    private String name;
    private String area;
    private ArrayList<String> skills;    // list of skills (e.g., "Cooking", "Java")
    private String password;             // stored plainly for this beginner project
    private int trustScore;              // initialised by TrustScoreManager
    private double timeCreditBalance;    // how many TimeCredits the user has


}
