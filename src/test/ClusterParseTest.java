package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import model.ModelCluster;

import org.yaml.snakeyaml.Yaml;

public class ClusterParseTest {
    public static void main(String[] args) throws FileNotFoundException {
        InputStream input = new FileInputStream(new File("data/large_cluster"));
        ModelCluster c = new Yaml().loadAs(input, ModelCluster.class);
        System.out.println(c);
    }
}
