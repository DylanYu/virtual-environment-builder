package action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RemoteExecute {
    public static void execute(String command, String host, String user, String pw) {
        try {
            execute(command, host, user, pw, 22);
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }
    
    // execute command with normal permission
    public static void execute(String command, String host, String user, String pw, int port) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(pw);

        // this is REQUIRED
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                channel.getInputStream()));
        channel.setCommand(command);
        channel.connect();
        
        String msg = null;
        while ((msg = in.readLine()) != null) {
            System.out.println(msg);
        }
        
        channel.disconnect();
        session.disconnect();
    }
    
    public static void sudoExecute(String command, String host, String user, String pw) {
        try {
            sudoExecute(command, host, user, pw, 22);
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }
    
    // execute command with root permission
    public static void sudoExecute(String command, String host, String user, String pw, int port) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(pw);
        // REQUIRED
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        
        String sudo_pass = pw;

        Channel channel = session.openChannel("exec");

        // man sudo
        // -S The -S (stdin) option causes sudo to read the password from the
        // standard input instead of the terminal device.
        // -p The -p (prompt) option allows you to override the default
        // password prompt and use a custom one.
        ((ChannelExec) channel).setCommand("sudo -S -p '' " + command);

        InputStream in = channel.getInputStream();
        OutputStream out = channel.getOutputStream();
        ((ChannelExec) channel).setErrStream(System.err);

        channel.connect();

        out.write((sudo_pass + "\n").getBytes());
        out.flush();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0)
                    break;
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }
        channel.disconnect();
        session.disconnect();
    }
    
    public static void main(String[] args) throws ConfigurationException, JSchException, IOException {
        Configuration config = new PropertiesConfiguration("host.properties");
//        String command = "apt-get install -y sshpass";
        String command = String.format("echo %s >> script.sh", "test");
        String host = config.getString("wshost.ip"); 
        String user = config.getString("wshost.user"); 
        String pw = config.getString("wshost.pw"); 
        
        execute(command, host, user, pw);
//        sudoExecute(command, host, user, pw);
    }
}
