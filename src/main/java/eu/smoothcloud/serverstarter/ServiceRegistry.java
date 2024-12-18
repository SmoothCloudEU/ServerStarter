package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry extends ServerTask {

    private final ConcurrentHashMap<UUID, Server> services = new ConcurrentHashMap<>();

    public void registerService(Server server) {
        UUID uniqueId = server.getUniqueId();
        if (services.putIfAbsent(uniqueId, server) != null) {
            return;
        }
        start(server);
    }

    public void unregisterService(Server server) {
        UUID uniqueId = server.getUniqueId();
        stop(server);
        services.remove(uniqueId);
    }

    public Server getService(UUID uniqueId) {
        return services.get(uniqueId);
    }
}

