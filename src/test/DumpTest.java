package test;

import org.yaml.snakeyaml.Yaml;

import model.ModelFlavor;
import model.ModelImage;
import model.ModelServer;

public class DumpTest {
    public static void main(String[] args) {
        ModelServer server = new ModelServer();
        ModelImage image = new ModelImage();
        image.setId("image_id_0");
        
        ModelFlavor flavor = new ModelFlavor();
        flavor.setId("flavor_id_1");
        flavor.setRam(2048);
        flavor.setVcpus(2);
        server.setImage(image);
        server.setFlavor(flavor);
        server.setId("server_id_1");
        server.setAddresses(new String[] {"192.168.1.78", "172.100.1.109"});
        
        Yaml yaml = new Yaml();
        String output = yaml.dump(server);
        System.out.println(output);
    }
}
