package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry extends ServerTask {
    private final ConcurrentHashMap<UUID, Server> services = new ConcurrentHashMap<>();

    public void registerService(Server server) {
        UUID uniqueId = server.getUniqueId();
        System.out.println("Registering service " + uniqueId);
        if (this.services.putIfAbsent(uniqueId, server) != null) {
            return;
        }
        this.start(uniqueId, server);
        System.out.println("Registered service " + uniqueId);
    }

    public void unregisterService(UUID uniqueId) {
        System.out.println("Unregistering service " + uniqueId);
        this.stop(uniqueId);
        this.services.remove(uniqueId);
        System.out.println("Unregistered service " + uniqueId);
    }

    public Server getService(UUID uniqueId) {
        return this.services.get(uniqueId);
    }
}

