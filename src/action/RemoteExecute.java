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
    public static int execute(String command, String host, String user, String pw) {
        try {
            return execute(command, host, user, pw, 22);
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // execute command with normal permission
    public static int execute(String command, String host, String user, String pw, int port) throws JSchException, IOException {
        /*
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
        */
        
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(pw);
        // REQUIRED
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        
        Channel channel = session.openChannel("exec");

        ((ChannelExec) channel).setCommand(command);

        InputStream in = channel.getInputStream();
        ((ChannelExec) channel).setErrStream(System.err);

        channel.connect();

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
            } catch (Exception e) {
            }
        }
        int exitStatus = channel.getExitStatus();
        channel.disconnect();
        session.disconnect();
        return exitStatus;
    }
    
    public static int sudoExecute(String command, String host, String user, String pw) {
        try {
            return sudoExecute(command, host, user, pw, 22);
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // execute command with root permission
    public static int sudoExecute(String command, String host, String user, String pw, int port) throws JSchException, IOException {
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
        int exitStatus = channel.getExitStatus();
        channel.disconnect();
        session.disconnect();
        return exitStatus;
    }
    
    public static int jumpExecute(String command, String jumpHost, String jumpUser, String jumpPw,
            String targetHost, String targetUser, String targetPw) {
        try {
            return jumpExecute(command,
                    jumpHost,
                    jumpUser,
                    jumpPw,
                    targetHost,
                    targetUser,
                    targetPw,
                    22);
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // ssh to jump box, then ssh from jump box to target machine, then execute command
    public static int jumpExecute(String command, String jumpHost, String jumpUser, String jumpPw,
            String targetHost, String targetUser, String targetPw, int port) throws JSchException, IOException {
        String finalCommand = String.format("sshpass -p '%s' ssh -o StrictHostKeyChecking=no %s@%s \"%s\"",
                targetPw,
                targetUser,
                targetHost,
                command);
        return execute(finalCommand, jumpHost, jumpUser, jumpPw, port);
    }
    
    public static int jumpSudoExecute(String command, String jumpHost, String jumpUser, String jumpPw,
            String targetHost, String targetUser, String targetPw) {
        try {
            return jumpSudoExecute(command,
                    jumpHost,
                    jumpUser,
                    jumpPw,
                    targetHost,
                    targetUser,
                    targetPw,
                    22);
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // use "root" as target user to gain sudo permission, which is different from sudoExecute()
    public static int jumpSudoExecute(String command, String jumpHost, String jumpUser, String jumpPw,
            String targetHost, String targetUser, String targetPw, int port) throws JSchException, IOException {
        String finalCommand = String.format(
                "sshpass -p '%s' ssh -o StrictHostKeyChecking=no %s@%s \"echo '%s' | sudo -S %s\"", // sudo -S read password from stand input
                targetPw,
                targetUser,
                targetHost,
                targetPw,
                command);
        return execute(finalCommand, jumpHost, jumpUser, jumpPw, port);
    }
    
    public static void main(String[] args) throws ConfigurationException, JSchException, IOException {
        Configuration config = new PropertiesConfiguration("host.properties");
        
//        String command = "apt-get install -y sshpass";
//        String command = String.format("echo %s >> script.sh", "test");
        String command = "ping -c1 114.212.189.120";
        String host = config.getString("wshost.ip"); 
        String user = config.getString("wshost.user"); 
        String pw = config.getString("wshost.pw");
        execute(command, host, user, pw);
//        sudoExecute(command, host, user, pw);
        
//        String command = String.format("echo %s >> script.sh", "test");
//        String jumpHost = config.getString("wshost.ip"); 
//        String jumpUser = config.getString("wshost.user"); 
//        String jumpPw = config.getString("wshost.pw");
////        String targetHost = config.getString("example_vm.ubuntu-desktop.ip");
//        String targetHost = "114.212.189.119";
//        String targetUser = config.getString("vm.ubuntu-desktop.user"); 
//        String targetPw = config.getString("vm.ubuntu-desktop.pw"); 
//        jumpExecute(command,
//            jumpHost,
//            jumpUser,
//            jumpPw,
//            targetHost,
//            targetUser,
//            targetPw);
        
//        String command = String.format("sleep 10; chmod 644 /home/nju/test.txt");
//        String command = String.format("echo 12345 >> test.txt");
//        String command = "apt-get remove tilda";
//        String command = "apt-get update";
//        String jumpHost = config.getString("wshost.ip"); 
//        String jumpUser = config.getString("wshost.user"); 
//        String jumpPw = config.getString("wshost.pw");
//        String targetHost = config.getString("wshost2.ip");
//        String targetUser = config.getString("wshost2.user");
//        String targetPw = config.getString("wshost2.pw"); 
//        jumpSudoExecute(command,
//            jumpHost,
//            jumpUser,
//            jumpPw,
//            targetHost,
//            targetUser,
//            targetPw);
    }
}
