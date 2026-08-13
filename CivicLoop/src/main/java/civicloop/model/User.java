package civicloop.model;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String userId;
    private String name;
    private String area;
    private ArrayList<String> skills;
    private String password;
    private int trustScore;
    private double timeCreditBalance;
    private String bio;   // new

    public User(String name, String area, String password, String userId) {
        this.userId = userId;
        this.name = name;
        this.area = area;
        this.password = password;
        this.skills = new ArrayList<>();
        this.trustScore = 50;
        this.timeCreditBalance = 0.0;
        this.bio = "";
    }

    public boolean checkPassword(String attempt) {
        return this.password.equals(attempt);
    }

    // Getters and setters (add new ones)
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public ArrayList<String> getSkills() { return skills; }
    public int getTrustScore() { return trustScore; }
    public void setTrustScore(int trustScore) { this.trustScore = Math.max(0, trustScore); }
    public double getTimeCreditBalance() { return timeCreditBalance; }
    public void setTimeCreditBalance(double balance) { this.timeCreditBalance = balance; }
    public void addSkill(String skill) { skills.add(skill); }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}