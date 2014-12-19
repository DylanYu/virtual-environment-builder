package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshTest {
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

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                channel.getInputStream()));
        //channel.setCommand("pwd;");
        channel.setCommand(String.format("echo %s >> scipt.sh", "test"));
        channel.connect();

        String msg = null;
        while ((msg = in.readLine()) != null) {
            System.out.println(msg);
        }
        
        channel.disconnect();
        session.disconnect();
    }
}
