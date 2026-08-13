package civicloop.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CommunityPost implements Serializable {
    private String postId;
    private String authorId;
    private String content;
    private String timestamp;
    private int likes;
    private ArrayList<String> comments;

    private CommunityPost(String postId, String authorId, String content) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        this.likes = 0;
        this.comments = new ArrayList<>();
    }

    public static CommunityPost createPost(String postId, String authorId, String content) {
        return new CommunityPost(postId, authorId, content);
    }

    public void addLike() { likes++; }
    public void addComment(String comment) { comments.add(comment); }

    public String getPostId() { return postId; }
    public String getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public int getLikes() { return likes; }
    public ArrayList<String> getComments() { return comments; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (♥ %d, 💬 %d)",
                timestamp, authorId, content, likes, comments.size());
    }
}