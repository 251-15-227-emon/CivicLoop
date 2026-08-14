package civicloop.model;

import java.io.Serializable;

public class TrustScoreManager implements Serializable {
    private String userId;
    private int score;

    public TrustScoreManager(String userId) {
        this.userId = userId;
        this.score = 50;
    }

    public void increaseScore(int amount) {
        score += amount;
        if (score > 100) score = 100;
    }

    public void decreaseScore(int amount) {
        score -= amount;
        if (score < 0) score = 0;
    }


    public int getScore() { return score; }
    public String getUserId() { return userId; }
    
}