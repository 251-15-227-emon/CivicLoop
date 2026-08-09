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
     
    public User(String name, String area, String password) {
        this.userId = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.area = area;
        this.password = password;
        this.skills = new ArrayList<>();
        this.trustScore = 50;            // starting trust score (see TrustScoreManager)
        this.timeCreditBalance = 0.0;
    }

    public boolean checkPassword(String attempt) {
        return this.password.equals(attempt);
    }



    // ---------- Public getters / controlled setters ----------
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getArea() { return area; }
    public ArrayList<String> getSkills() { return skills; }
    public int getTrustScore() { return trustScore; }
    public void setTrustScore(int trustScore) { this.trustScore = Math.max(0, trustScore); }
    public double getTimeCreditBalance() { return timeCreditBalance; }
    public void setTimeCreditBalance(double balance) { this.timeCreditBalance = balance; }

    public void addSkill(String skill) { skills.add(skill); }

}


