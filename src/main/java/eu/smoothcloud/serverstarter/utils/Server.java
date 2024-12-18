package eu.smoothcloud.serverstarter.utils;

import java.util.UUID;

public class Server {

    private final UUID uniqueId;
    private final String name;
    private String template;
    private String address;
    private int port;
    private String javaPath;
    private String serverSoftware;

    private int servicePriority;
    private String permission;

    private int minimumOnlineServices;
    private int maximumOnlineServices;
    private int newServicePercentage;

    private int minimumMemory;
    private int maximumMemory;

    private boolean proxy;
    private boolean maintenance;
    private boolean staticService;

    public Server(UUID uniqueId, String name, String template, String address, int port, String javaPath, String serverSoftware, int servicePriority, String permission, int minimumOnlineServices, int maximumOnlineServices, int newServicePercentage, int minimumMemory, int maximumMemory, boolean maintenance, boolean staticService) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.template = template;
        this.address = address;
        this.port = port;
        this.javaPath = javaPath;
        this.serverSoftware = serverSoftware;
        this.servicePriority = servicePriority;
        this.permission = permission;
        this.minimumOnlineServices = minimumOnlineServices;
        this.maximumOnlineServices = maximumOnlineServices;
        this.newServicePercentage = newServicePercentage;
        this.minimumMemory = minimumMemory;
        this.maximumMemory = maximumMemory;
        this.maintenance = maintenance;
        this.staticService = staticService;
    }

    public Server(UUID uniqueId, String name, String template, String address, int port) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.template = template;
        this.address = address;
        this.port = port;
    }

    public Server(String name, String template, int port) {
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.template = template;
        this.port = port;
    }

    public Server(UUID uniqueId, String name, String template, int port) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.template = template;
        this.port = port;
    }


    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public String getServerSoftware() {
        return serverSoftware;
    }

    public void setServerSoftware(String serverSoftware) {
        this.serverSoftware = serverSoftware;
    }

    public int getServicePriority() {
        return servicePriority;
    }

    public void setServicePriority(int servicePriority) {
        this.servicePriority = servicePriority;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public int getMinimumOnlineServices() {
        return minimumOnlineServices;
    }

    public void setMinimumOnlineServices(int minimumOnlineServices) {
        this.minimumOnlineServices = minimumOnlineServices;
    }

    public int getMaximumOnlineServices() {
        return maximumOnlineServices;
    }

    public void setMaximumOnlineServices(int maximumOnlineServices) {
        this.maximumOnlineServices = maximumOnlineServices;
    }

    public int getNewServicePercentage() {
        return newServicePercentage;
    }

    public void setNewServicePercentage(int newServicePercentage) {
        this.newServicePercentage = newServicePercentage;
    }

    public int getMinimumMemory() {
        return minimumMemory;
    }

    public void setMinimumMemory(int minimumMemory) {
        this.minimumMemory = minimumMemory;
    }

    public int getMaximumMemory() {
        return maximumMemory;
    }

    public void setMaximumMemory(int maximumMemory) {
        this.maximumMemory = maximumMemory;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public boolean isStaticService() {
        return staticService;
    }

    public void setStaticService(boolean staticService) {
        this.staticService = staticService;
    }

    public boolean isProxy() {
        return serverSoftware.contains("bungeecord") || serverSoftware.contains("waterfall") || serverSoftware.contains("velocity");
    }
}
