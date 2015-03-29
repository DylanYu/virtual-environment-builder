package action;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;
/**
 * Singleton
 * 
 * @author Dongliang Yu
 *
 */
public class OS {
    private static volatile OSClient instance;
    private OS() {}
    
    public static OSClient getClient () {
        if (instance == null) {
            synchronized (OS.class) {
                if (instance == null) {
                    Configuration apiConfig = null;
                    try {
                        apiConfig = new PropertiesConfiguration("api.properties");
                        instance = OSFactory.builder()
                                .endpoint(apiConfig.getString("auth_uri"))
                                .credentials(apiConfig.getString("user"),apiConfig.getString("password"))
                                .tenantName(apiConfig.getString("tenant"))
                                .authenticate();
                    } catch (ConfigurationException e) {
                        System.out.println("Cannot read api.properties");
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }
}
