package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshSudoTest {
    public static void main(String[] args) throws JSchException, IOException {
        String user = "vagrant";
        String password = "vagrant";
        String host = "127.0.0.1";
        int port = 2222;

        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        // REQUIRED
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        String command = "apt-get install sshpass";

        String sudo_pass = password;

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
}
