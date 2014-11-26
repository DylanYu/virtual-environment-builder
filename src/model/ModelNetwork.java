package model;

public class ModelNetwork {
    private String id;
    private String name;
    private String CIDR;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCIDR() {
        return CIDR;
    }
    public void setCIDR(String CIDR) {
        this.CIDR = CIDR;
    }
    
    public String toString() {
        return String.format("{Network id: %s, Name: %s, CIDR: %s}", this.id, this.name, this.CIDR);
    }
}
