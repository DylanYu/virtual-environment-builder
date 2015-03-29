package action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ScpFile {
    public static void scp(String srcHost, String srcUser, String srcPw, String srcFilePath,
            String dstHost, String dstUser, String dstPw, String dstFilePath) throws JSchException, IOException {
        scp(srcHost, srcUser, srcPw, srcFilePath,
                dstHost, dstUser, dstPw, dstFilePath, 22);
    }
    
    public static void scp(String srcHost, String srcUser, String srcPw, String srcFilePath,
            String dstHost, String dstUser, String dstPw, String dstFilePath, int port) throws JSchException, IOException {
        scpInSrc(srcHost, srcUser, srcPw, srcFilePath,
                dstHost, dstUser, dstPw, dstFilePath, port);
    }
    
    // first ssh to src host, then execute scp command
    public static void scpInSrc(String srcHost, String srcUser, String srcPw, String srcFilePath,
            String dstHost, String dstUser, String dstPw, String dstFilePath, int port) throws JSchException, IOException {
        String command = String.format("sshpass -p '%s' scp -r -o StrictHostKeyChecking=no %s %s@%s:%s",
                dstPw,
                srcFilePath,
                dstUser,
                dstHost,
                dstFilePath);
        RemoteExecute.execute(command,
                srcHost,
                srcUser,
                srcPw,
                22);
    }
    
    // first ssh to dst host, then execute scp command
    public static void scpInDst(String srcHost, String srcUser, String srcPw, String srcFilePath,
            String dstHost, String dstUser, String dstPw, String dstFilePath, int port) throws JSchException, IOException {
        String command = String.format("sshpass -p '%s' scp -r -o StrictHostKeyChecking=no %s@%s:%s %s",
                srcPw,
                srcUser,
                srcHost,
                srcFilePath,
                dstFilePath);
        RemoteExecute.execute(command,
                dstHost,
                dstUser,
                dstPw,
                22);
    }
    
    public static void main(String[] args) throws JSchException, IOException, ConfigurationException {
        Configuration config = new PropertiesConfiguration("host.properties");
        System.out.println("Transfering cookbooks to server...");
        ScpFile.scp(config.getString("oshost.ip"), 
                config.getString("oshost.user"), 
                config.getString("oshost.pw"), 
                config.getString("oshost.cookbook.apache2.path"), 
                "114.212.189.122", 
                config.getString("vm.ubuntu-desktop.user"), 
                config.getString("vm.ubuntu-desktop.pw"), 
                config.getString("vm.cookbook.path"));
        System.out.println("Transfer completed");
        
        /*
        String srcHost = config.getString("wshost.ip"); 
        String srcUser = config.getString("wshost.user"); 
        String srcPw = config.getString("wshost.pw");
//        String srcFilePath = config.getString("wshost.chef_client.path");
        String srcFilePath = "/home/nju/cookbooks/learn_chef_apache2";
        String dstHost = config.getString("oshost.ip"); 
        String dstUser = config.getString("oshost.user"); 
        String dstPw = config.getString("oshost.pw"); 
//        String dstHost = "114.212.189.117";
//        String dstUser = config.getString("vm.ubuntu-desktop.user"); 
//        String dstPw = config.getString("vm.ubuntu-desktop.pw"); 
//        String dstFilePath = config.getString("dst.chef_client.path");
        String dstFilePath = "~/";
        scp(srcHost,
            srcUser,
            srcPw,
            srcFilePath,
            dstHost,
            dstUser,
            dstPw,
            dstFilePath);
//        scpInDst(srcHost,
//                srcUser,
//                srcPw,
//                srcFilePath,
//                dstHost,
//                dstUser,
//                dstPw,
//                dstFilePath,
//                port);
         */
    }
}
