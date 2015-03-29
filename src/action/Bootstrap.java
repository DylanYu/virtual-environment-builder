package action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

import util.Network;

import com.jcraft.jsch.JSchException;

public class Bootstrap {
    public static void main (String[] args) throws ConfigurationException, JSchException, IOException {
        Configuration hostConfig = new PropertiesConfiguration("host.properties");
        Configuration resourceIdConfig = new PropertiesConfiguration("resource_id.properties");
        
        // Clean up instances
        Set<String> retainedServersId = new HashSet<String>(); 
        retainedServersId.add(resourceIdConfig.getString("server.test-server.id"));
        retainedServersId.add(resourceIdConfig.getString("server.occupy-server1.id"));
        retainedServersId.add(resourceIdConfig.getString("server.occupy-server2.id"));
        CleanUp.cleanInstances(retainedServersId);
        
//        InputStream input = new FileInputStream(new File("data/large_cluster"));
//        InputStream input = new FileInputStream(new File("data/small_cluster"));
        InputStream input = new FileInputStream(new File(args[1])); // index [1] because we need to use [0] to define main class entry
        ModelCluster cluster = new Yaml().loadAs(input, ModelCluster.class);
        System.out.println(cluster);
        
        OSClient os = OS.getClient();
//        Flavor flavor = os.compute().flavors().get(resourceIdConfig.getString("flavor.tiny.id"));
//        Image image = os.images().get(resourceIdConfig.getString("image.cirros.id"));
        Flavor flavor = os.compute().flavors().get(resourceIdConfig.getString("flavor.small.id"));
        Image image = os.images().get(resourceIdConfig.getString("image.ubuntu-desktop.id"));
        
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
        
        // scp necessary files
        while (os.compute().servers().get(servers[0].getId()).getStatus() != Server.Status.ACTIVE) {
            System.out.println(String.format("wait for server %s booting", servers[0].getName()));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(String.format("server %s is ready", servers[0].getName()));
        // find address for the server
        Address serverAddress = null;
        List<? extends Address> addresses = servers[0].getAddresses().getAddresses("private");
        for (Address address : addresses) {
            // Equals, not ==
            if (address.getType().equals("floating")) {// skip fixed address
                serverAddress = address;
                break;
            }
        }
        System.out.println(serverAddress.getAddr());
        
        // test whether server is reachable
        boolean reachable = Network.remotePingTest(
                hostConfig.getString("oshost.ip"), 
                hostConfig.getString("oshost.user"), 
                hostConfig.getString("oshost.pw"), 
                serverAddress.getAddr());
//        System.out.println("ping test passed? " + reachable);
        
        System.out.println("Transfering necessary files to server...");
        ScpFile.scp(hostConfig.getString("oshost.ip"), 
                hostConfig.getString("oshost.user"), 
                hostConfig.getString("oshost.pw"), 
                hostConfig.getString("oshost.chef_client.path"), 
                serverAddress.getAddr(), 
                hostConfig.getString("vm.ubuntu-desktop.user"), 
                hostConfig.getString("vm.ubuntu-desktop.pw"), 
                hostConfig.getString("dst.chef_client.path"));
        System.out.println("Transfer completed");
        
        System.out.println("Installing Chef-client...");
        String command = String.format("dpkg -i %s/%s", 
                hostConfig.getString("dst.chef_client.path"), 
                hostConfig.getString("file.chef_client.name"));
        RemoteExecute.jumpSudoExecute(
                command,
                hostConfig.getString("oshost.ip"), 
                hostConfig.getString("oshost.user"), 
                hostConfig.getString("oshost.pw"), 
                serverAddress.getAddr(), 
                hostConfig.getString("vm.ubuntu-desktop.user"), 
                hostConfig.getString("vm.ubuntu-desktop.pw"));
        System.out.println("Installation completed");
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
