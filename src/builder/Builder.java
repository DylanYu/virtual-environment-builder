package builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import model.ModelCluster;
import model.ModelNetwork;
import model.ModelServer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Router;
import org.openstack4j.model.network.RouterInterface;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.OSFactory;
import org.yaml.snakeyaml.Yaml;

public class Builder {
    public static void main(String[] args) throws FileNotFoundException, ConfigurationException {
        Configuration apiConfig = new PropertiesConfiguration("api.properties");
        Configuration resourceIdConfig = new PropertiesConfiguration("resource_id.properties");
        
//        InputStream input = new FileInputStream(new File("data/large_cluster"));
//        InputStream input = new FileInputStream(new File("data/small_cluster"));
        InputStream input = new FileInputStream(new File(args[1])); // index [1] because we need to use [0] to define main class entry
        ModelCluster cluster = new Yaml().loadAs(input, ModelCluster.class);
        System.out.println(cluster);
        
        OSClient os = OSFactory.builder()
                .endpoint(apiConfig.getString("auth_uri"))
                .credentials(apiConfig.getString("user"),apiConfig.getString("password"))
                .tenantName(apiConfig.getString("tenant"))
                .authenticate();
        Tenant tenant = os.identity().tenants().getByName(apiConfig.getString("tenant"));
        
        /*
        //Router router = os.networking().router().get(resourceIDConfig.getString("router.id"));
        ModelNetwork[] modelNetworks = cluster.getCluster_networks();
        Network[] networks = new Network[modelNetworks.length];
        for (int i = 0; i < modelNetworks.length; i++) {
            ModelNetwork mNet = modelNetworks[i];
            networks[i] = os.networking().network().create(Builders.network()
                    .name(mNet.getName())
                    .tenantId(tenant.getId())
                    .build());
            Subnet subnet = Builders.subnet()
                            .networkId(networks[i].getId())
                            .tenantId(tenant.getId())
                            .cidr(mNet.getCIDR())
                            .ipVersion(IPVersionType.V4) // required
                            .build();
            os.networking().subnet().create(subnet);
        }
        */
        
        Flavor flavor = os.compute().flavors().get(resourceIdConfig.getString("flavor.tiny.id"));
        Image image = os.images().get(resourceIdConfig.getString("image.cirros.id"));
        
        // Define networks where instances will be attached to
        List<String> nets = new LinkedList<String>();
        /*
        for (Network createdNet : networks) {
            for (ModelNetwork selfNetwork : mServer.getNetworks()) {
                if (selfNetwork.getName().equals(createdNet.getName()))
                    nets.add(createdNet.getId());
            }
        }*/
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
    }
}