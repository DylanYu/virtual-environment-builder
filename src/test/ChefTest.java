package test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.common.net.HostAndPort;

public class ChefTest {
    public static void main(String[] args) throws IOException {
        /*
     // Get the credentials that will be used to authenticate to the Chef server
        String chefVersion = "chef";
        
        String client = "dongliangyu";
        String organization = "nju_cloud";
        String pemFile = "credential/" + client + ".pem";
        String credential = Files.toString(new File(pemFile), Charsets.UTF_8);

        // Provide the validator information to let the nodes to auto-register themselves
        // in the Chef server during bootstrap
        String validator = organization + "-validator";
        String validatorPemFile = "credential/" + validator + ".pem";
        String validatorCredential = Files.toString(new File(validatorPemFile), Charsets.UTF_8);

        Properties chefConfig = new Properties();
        chefConfig.put(ChefProperties.CHEF_VALIDATOR_NAME, validator);
        chefConfig.put(ChefProperties.CHEF_VALIDATOR_CREDENTIAL, validatorCredential);

        // Create the connection to the Chef server
        ChefContext chefContext = ContextBuilder.newBuilder(chefVersion)
            .endpoint("https://dylab/organizations/" + organization)
            .credentials(client, credential)
            .overrides(chefConfig)
            .buildView(ChefContext.class);

        // Create the connection to the compute provider. Note that ssh will be used to bootstrap chef
//        ComputeServiceContext computeContext = ContextBuilder.newBuilder("<the compute provider name>")
//            .endpoint("<the compute endpoint>")
//            .credentials("<identity>", "<credential>")
//            .modules(ImmutableSet.<Module> of(new SshjSshClientModule()))
//            .buildView(ComputeServiceContext.class);

        // Group all nodes in both Chef and the compute provider by this group
        String group = "clients";

        // Set the recipe to install and the configuration values to override
        String recipe = "starter2";
        //String attributes = "{\"apache\": {\"listen_ports\": \"8080\"}}";

        // Check to see if the recipe you want exists
        List<String> runlist = new RunListBuilder().addRecipe(recipe).build();

        // Update the chef service with the run list you wish to apply to all nodes in the group
        // and also provide the json configuration used to customize the desired values
        BootstrapConfig config = BootstrapConfig.builder().runList(runlist).build();
        chefContext.getChefService().updateBootstrapConfigForGroup(group, config);

        // Build the script that will bootstrap the node
        Statement bootstrap = chefContext.getChefService().createBootstrapScriptForGroup(group);
        
        StringBuilder rawScript = new StringBuilder();
        Map<String, String> resolvedFunctions = ScriptBuilder.resolveFunctionDependenciesForStatements(
            new HashMap<String, String>(), ImmutableSet.of(bootstrap), OsFamily.UNIX);
        ScriptBuilder.writeFunctions(resolvedFunctions, OsFamily.UNIX, rawScript);
        rawScript.append(bootstrap.render(OsFamily.UNIX));
        System.out.println(rawScript.toString());
        
//        String nodeName = "controller";
//        List<String> runlist = new RunListBuilder().addRecipe("starter2").build();
//        // TODO will register this node, which is not what we want
//        Node node = context.getChefService().createNodeAndPopulateAutomaticAttributes(nodeName, runlist);

        chefContext.close();
        */
    }
}
