package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshScpTest {
    public static void main(String[] args) throws JSchException, IOException {
        JSch jsch = new JSch();
        String remoteHost = "127.0.0.1";
        int port = 2222;
        String user = "vagrant";
        String password = "vagrant";
        Session session = jsch.getSession(user, remoteHost, port);
        session.setPassword(password);

        // this is REQUIRED
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        String fileFrom = "~/Chef/downloads/chef_12.0.3-1_amd64.deb";
        //String fileFrom = "~/Chef/downloads/testfile";
        String fileTo = "~/";
        
        String fileSrcIp = "114.212.81.193";
        String fileSrcUsr = "dylan";
        // TODO modify
        String fileSrcPw = "";
        
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                channel.getInputStream()));
        String command = String.format("sshpass -p '%s' scp -o StrictHostKeyChecking=no %s@%s:%s %s",
                fileSrcPw,
                fileSrcUsr,
                fileSrcIp,
                fileFrom,
                fileTo);
        channel.setCommand(command);
        channel.connect();

        String msg = null;
        while ((msg = in.readLine()) != null) {
            System.out.println(msg);
        }
        
        channel.disconnect();
        session.disconnect();
    }
}
