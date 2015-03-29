package action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import model.ModelCluster;
import model.ModelServer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;
import org.yaml.snakeyaml.Yaml;

public class Bootstrap {
    public static void main (String[] args) throws ConfigurationException, FileNotFoundException {
        Configuration resourceIdConfig = new PropertiesConfiguration("resource_id.properties");
        
//        InputStream input = new FileInputStream(new File("data/large_cluster"));
//        InputStream input = new FileInputStream(new File("data/small_cluster"));
        InputStream input = new FileInputStream(new File(args[1])); // index [1] because we need to use [0] to define main class entry
        ModelCluster cluster = new Yaml().loadAs(input, ModelCluster.class);
        System.out.println(cluster);
        
        OSClient os = OS.getClient();
        Flavor flavor = os.compute().flavors().get(resourceIdConfig.getString("flavor.tiny.id"));
        Image image = os.images().get(resourceIdConfig.getString("image.cirros.id"));
        
        // Define networks where instances will be attached to
        List<String> nets = new LinkedList<String>();
        nets.add(resourceIdConfig.getString("network.network1.id"));
        
        ModelServer[] modelServers = cluster.getServers();
        Server[] servers = new Server[modelServers.length];
        for (int i = 0; i < modelServers.length; i++) {
            ModelServer mServer = modelServers[i];
            servers[i] = os.compute().servers().boot(Builders.server()
                    .name(mServer.getId())
                    .flavor(flavor.getId())
                    .image(image.getId())
                    .networks(nets)
                    .build());
        }
        
        // Assign floating IP
        try { // MUST wait for Nova, otherwise may fail in assigning floating IP 
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < servers.length; i++) {
            Server server = servers[i];
            if (!hasAvailableFloatingIP()) {
                String poolName = os.compute().floatingIps().getPoolNames().get(0);
                System.out.println(String.format("Allocate floating IP in pool %s", poolName));
                os.compute().floatingIps().allocateIP(poolName);
            }
            
            List<FloatingIP> ips = getAvailableFloatingIP();
            if (ips.size() == 0) {
                System.out.println("error: no available floating ip");
                return;
            }
            FloatingIP ip = ips.get(0);
            ActionResponse r = os.compute().floatingIps().addFloatingIP(
                    server, 
                    ip.getFloatingIpAddress()); //
            System.out.println(
                    String.format("associate floating IP %s success? %b, fault msg: %s", 
                            ip.getFloatingIpAddress(), 
                            r.isSuccess(),
                            r.getFault()));
            String id = servers[i].getId();
            servers[i] = OS.getClient().compute().servers().get(id); // MUST update model
            List<? extends Address> addresses = servers[i].getAddresses().getAddresses("private");
            for (Address address : addresses) {
                // Equals, not ==
                if (address.getType().equals("floating")) // skip fixed address
                    System.out.println("assigned:" + address);
            }
        }
    }
    
    private static boolean hasAvailableFloatingIP() throws ConfigurationException {
        List<? extends FloatingIP> floatingIPs = OS.getClient().compute().floatingIps().list();
        for (FloatingIP ip : floatingIPs) {
            if (ip.getFixedIpAddress() == null)
                return true;
        }
        return false;
    }
    
    private static List<FloatingIP> getAvailableFloatingIP () throws ConfigurationException {
        List<? extends FloatingIP> allFloatingIPs = OS.getClient().compute().floatingIps().list();
        List<FloatingIP> availableFloatingIPs = new LinkedList<FloatingIP>();
        for (FloatingIP ip : allFloatingIPs) {
            if (ip.getFixedIpAddress() == null)
                availableFloatingIPs.add(ip);
        }
        return availableFloatingIPs;
    }
}
