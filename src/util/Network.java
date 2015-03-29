package util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import action.RemoteExecute;

public class Network {
    // TODO add timeout
    public static boolean remotePingTest(String jumpHost, String jumpUser, String jumpPw, String targetHost) {
        while (0 != RemoteExecute.execute(
                    String.format("ping -c1 %s", targetHost), jumpHost, jumpUser, jumpPw)) {
            try {
                System.out.println(String.format("try to ping host %s...", targetHost));
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        System.out.println("ping test passed");
        return true;
    }
}
