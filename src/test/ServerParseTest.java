package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import model.ModelServer;

import org.yaml.snakeyaml.Yaml;

public class ServerParseTest {
    public static void main(String[] args) throws FileNotFoundException {
        InputStream input = new FileInputStream(new File("data/single_server"));
        ModelServer s = new Yaml().loadAs(input, ModelServer.class);
        System.out.println(s);
    }
}
