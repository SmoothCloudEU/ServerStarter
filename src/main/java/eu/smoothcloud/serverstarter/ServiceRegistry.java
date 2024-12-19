package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceRegistry extends ServerTask {

    private final ConcurrentHashMap<UUID, Server> services = new ConcurrentHashMap<>();

    /**
     * Registers a service. If the service is already registered, no action is taken.
     *
     * @param server The server instance to register.
     */
    public void registerService(Server server) {
        UUID uniqueId = server.getUniqueId();
        System.out.printf("Attempting to register service with ID: %s%n", uniqueId);

        if (services.putIfAbsent(uniqueId, server) == null) {
            start(uniqueId, server);
            System.out.printf("Successfully registered service with ID: %s%n", uniqueId);
            return;
        }
        System.out.printf("Service with ID: %s is already registered.%n", uniqueId);
    }

    /**
     * Unregisters a service. If the service is not registered, no action is taken.
     *
     * @param uniqueId The unique ID of the server to unregister.
     */
    public void unregisterService(UUID uniqueId) {
        System.out.printf("Attempting to unregister service with ID: %s%n", uniqueId);

        if (services.remove(uniqueId) != null) {
            stop(uniqueId);
            System.out.printf("Successfully unregistered service with ID: %s%n", uniqueId);
            return;
        }
        System.out.printf("No service found with ID: %s to unregister.%n", uniqueId);
    }

    /**
     * Retrieves a registered service by its unique ID.
     *
     * @param name The name of the server.
     * @return The server instance if found, otherwise null.
     */
    public Server getService(String name) {
        AtomicReference<Server> atomicServer = new AtomicReference<>();
        for (Server server : this.services.values()) {
            if (server.getName().equalsIgnoreCase(name)) {
                atomicServer.set(server);
                break;
            }
        }
        return atomicServer.get();
    }

    public ConcurrentHashMap<UUID, Server> getServices() {
        return services;
    }
}
