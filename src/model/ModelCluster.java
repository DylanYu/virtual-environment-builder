package model;

public class ModelCluster {
    private ModelNetwork[] cluster_networks;
    private ModelServer[] servers;
    // flavor & image
    public ModelNetwork[] getCluster_networks() {
        return cluster_networks;
    }
    public void setCluster_networks(ModelNetwork[] cluster_networks) {
        this.cluster_networks = cluster_networks;
    }
    public ModelServer[] getServers() {
        return servers;
    }
    
    public void setServers(ModelServer[] servers) {
        this.servers = servers;
    }
    
    public String toString() {
        String networkStr = "";
        for (ModelNetwork net : cluster_networks)
            networkStr += "- " + net + "\n";
        String serverStr = "";
        for (ModelServer server : servers)
            serverStr += "- " + server + "\n";
        return String.format("Cluster::\n" +
        		"Networks:\n" +
        		"%s" +
        		"Servers:\n" +
        		"%s", 
        		networkStr,
        		serverStr);
    }
}
