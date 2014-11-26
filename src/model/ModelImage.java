package model;

public class ModelImage {
    private String id;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return String.format("{Image ID: %s}", id);
    }
}
