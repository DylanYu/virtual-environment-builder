package builder;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.openstack.OSFactory;

public class Floating {
    public static void main(String[] args) throws ConfigurationException {
        
        Configuration apiConfig = new PropertiesConfiguration("api.properties");
        Configuration resourceIdConfig = new PropertiesConfiguration("resource_id.properties");
        OSClient os = OSFactory.builder()
                .endpoint(apiConfig.getString("auth_uri"))
                .credentials(apiConfig.getString("user"),apiConfig.getString("password"))
                .tenantName(apiConfig.getString("tenant"))
                .authenticate();
        //Tenant tenant = os.identity().tenants().getByName(globalConfig.getString("tenant"));
        
        List<String> pools = os.compute().floatingIps().getPoolNames();
        for (String e : pools)
            System.out.println("pool:" + e);
        
        //FloatingIP ip = os.compute().floatingIps().allocateIP("nova");
        //System.out.println(ip.toString());
        
        Server server = os.compute().servers().get(
                resourceIdConfig.getString("server.test-server.id"));
        ActionResponse r = os.compute().floatingIps().addFloatingIP(
                server, 
                resourceIdConfig.getString("floatingip.test-ip.ip")); //
        System.out.println("associate success? " + r.isSuccess());
        
        //os.compute().floatingIps().deallocateIP(ip.getId());
    }
}