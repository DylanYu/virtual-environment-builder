package test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.chef.ChefApi;
import org.jclouds.chef.ChefContext;
import org.jclouds.chef.config.ChefProperties;
import org.jclouds.chef.domain.Node;
import org.jclouds.chef.util.RunListBuilder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ChefTest {
    public static void main(String[] args) throws IOException {
        String chefVersion = "chef";
        //String chefVersion = "enterprisechef";
        
        String client = "dongliangyu";
        String organization = "nju_cloud";
        String pemFile = "credential/" + client + ".pem";
        String credential = Files.toString(new File(pemFile), Charsets.UTF_8);
        
//        String validator = organization + "-validator";
//        String validatorPemFile = "credential/" + validator + ".pem";
//        String validatorCredential = Files.toString(new File(validatorPemFile), Charsets.UTF_8);
//        
//        Properties chefConfig = new Properties();
//        chefConfig.put(ChefProperties.CHEF_VALIDATOR_NAME, validator);
//        chefConfig.put(ChefProperties.CHEF_VALIDATOR_CREDENTIAL, validatorCredential);
        
        
        ChefContext context = ContextBuilder.newBuilder(chefVersion)
                .endpoint("https://dylab/organizations/" + organization)
                .credentials(client, credential)
//                .overrides(chefConfig)
                .buildView(ChefContext.class);
        
        //String nodeName = "mypc";
        String nodeName = "controller";
        List<String> runlist = new RunListBuilder().addRecipe("starter2").build();
        //context.getChefService().updateAutomaticAttributesOnNode(nodeName);
        // TODO will register this node, which is not what we want
        Node node = context.getChefService().createNodeAndPopulateAutomaticAttributes(nodeName, runlist);

        context.close();
    }
}
