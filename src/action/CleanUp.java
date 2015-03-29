package action;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.Server;

public class CleanUp {
    public static void cleanInstances(Set<String> retainedServersId) {
        for (Server server : OS.getClient().compute().servers().list()) {
            if (retainedServersId.contains(server.getId()))
                continue;
            ActionResponse r = OS.getClient().compute().servers().delete(server.getId());
            if (!r.isSuccess()) {
                System.out.println(
                        String.format("Delete instance %s(%s) failed", 
                                server.getName(), 
                                server.getId()));
            }
        }
    }
    
    public static void main(String[] args) throws ConfigurationException {
        Configuration config = new PropertiesConfiguration("resource_id.properties");
        Set<String> retainedServersId = new HashSet<String>(); 
        retainedServersId.add(config.getString("server.test-server.id"));
        retainedServersId.add(config.getString("server.occupy-server1.id"));
        retainedServersId.add(config.getString("server.occupy-server2.id"));
        cleanInstances(retainedServersId);
    }
}
