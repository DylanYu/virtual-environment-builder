package model;

public class ModelServer {
    private ModelImage image;
    private ModelFlavor flavor;
    private ModelNetwork[] networks;
    private String id;
    // TODO networks are for creation while addresses are for operation
    private String[] addresses;
    public ModelImage getImage() {
        return image;
    }
    public void setImage(ModelImage image) {
        this.image = image;
    }
    public ModelFlavor getFlavor() {
        return flavor;
    }
    public void setFlavor(ModelFlavor flavor) {
        this.flavor = flavor;
    }
    public ModelNetwork[] getNetworks() {
        return networks;
    }
    public void setNetworks(ModelNetwork[] networks) {
        this.networks = networks;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String[] getAddresses() {
        return addresses;
    }
    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }
    
    public String toString() {
        String addrs = "";
        for (String a : addresses)
            addrs += (a + ",");
        String nets = "";
        for (ModelNetwork net : networks)
            nets += (net + ",");
        return String.format("Server: {\n" +
        		"\tID: %s\n" +
        		"\tIP_addresses: [%s]\n" +
        		"\tNetworks: %s\n" + 
        		"\tFlavor: %s\n" +
        		"\tImage: %s\n}",
                this.id,
                addrs,
                nets,
                this.flavor,
                this.image);
    }
}
