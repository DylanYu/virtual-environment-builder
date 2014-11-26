package model;

public class ModelFlavor {
    private String id;
    private String name;
    private int vcpus;
    private int ram;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getVcpus() {
        return vcpus;
    }
    public void setVcpus(int vcpus) {
        this.vcpus = vcpus;
    }
    public int getRam() {
        return ram;
    }
    public void setRam(int ram) {
        this.ram = ram;
    }
    public String toString() {
        return String.format("{Flavor id: %s, vCPUs: %d, RAM: %d}", this.id, this.vcpus, this.ram);
    }
}
