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
import org.openstack4j.openstack.OSFactory;
import org.yaml.snakeyaml.Yaml;

public class Builder {
    public static void main(String[] args) throws FileNotFoundException, ConfigurationException {
        Configuration globalConfig = new PropertiesConfiguration("global.properties");
        
        InputStream input = new FileInputStream(new File("data/large_cluster"));
//        InputStream input = new FileInputStream(new File("data/small_cluster"));
        ModelCluster cluster = new Yaml().loadAs(input, ModelCluster.class);
        System.out.println(cluster);
        
        OSClient os = OSFactory.builder()
                .endpoint(globalConfig.getString("auth_uri"))
                .credentials(globalConfig.getString("user"),globalConfig.getString("password"))
                .tenantName(globalConfig.getString("tenant"))
                .authenticate();
        Tenant tenant = os.identity().tenants().getByName(globalConfig.getString("tenant"));
        
        ModelNetwork[] modelNetworks = cluster.getCluster_networks();
        Network[] networks = new Network[modelNetworks.length];
        for (int i = 0; i < modelNetworks.length; i++) {
            ModelNetwork mNet = modelNetworks[i];
            networks[i] = os.networking().network().create(Builders.network()
                    .name(mNet.getName())
                    .tenantId(tenant.getId())
                    .build());
            os.networking().subnet().create(Builders.subnet()
                    .networkId(networks[i].getId())
                    .tenantId(tenant.getId())
                    .cidr(mNet.getCIDR())
                    .ipVersion(IPVersionType.V4) // required
                    .build());
        }
        
        Configuration testConfig = new PropertiesConfiguration("test.properties");
        Flavor flavor = os.compute().flavors().get(testConfig.getString("flavor.tiny.id"));
        Image image = os.images().get(testConfig.getString("image.cirros.id"));
        
        ModelServer[] modelServers = cluster.getServers();
        Server[] servers = new Server[modelServers.length];
        for (int i = 0; i < modelServers.length; i++) {
            ModelServer mServer = modelServers[i];
            List<String> nets = new LinkedList<String>();
            for (Network createdNet : networks) {
                for (ModelNetwork selfNetwork : mServer.getNetworks()) {
                    if (selfNetwork.getName().equals(createdNet.getName()))
                        nets.add(createdNet.getId());
                }
            }
            servers[i] = os.compute().servers().boot(Builders.server()
                    .name(mServer.getId())
                    .flavor(flavor.getId())
                    .image(image.getId())
                    .networks(nets)
                    .build());
        }
    }
}
