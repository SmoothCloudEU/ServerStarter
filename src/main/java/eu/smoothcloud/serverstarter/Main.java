package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static final ServiceRegistry registry = new ServiceRegistry();

    public static void main(String[] args) {
        Server server = new Server(UUID.randomUUID(), "Lobby-1", "Lobby", "", 8084, "", "server.jar", 10, "", 1, 1, 95, 128, 1024, false, false);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("serverstarter » ");
                String command = scanner.nextLine().trim();

                // Check for empty input
                if (command.isEmpty()) {
                    System.out.println("Please enter a valid command.");
                    continue;
                }

                String[] parts = command.split(" ", 2); // Split command into action and arguments
                String action = parts[0].toLowerCase();
                String arguments = parts.length > 1 ? parts[1].trim() : "";

                switch (action) {
                    case "start" -> registry.registerService(server);

                    case "shutdown" -> {
                        if (registry.getService(server.getUniqueId()) != null) {
                            registry.unregisterService(server.getUniqueId());
                            continue;
                        }
                        System.out.println("Server is not running.");
                    }
                    case "exec" -> {
                        if (arguments.isEmpty()) {
                            System.out.println("Please provide a command to execute.");
                            continue;
                        }
                        registry.execute(server.getUniqueId(), arguments.replace("-", " "));

                    }

                    case "log" -> registry.showLogs(server.getUniqueId());

                    case "ex" -> {
                        if (registry.getService(server.getUniqueId()) != null) {
                            registry.unregisterService(server.getUniqueId());
                        }
                        System.out.println("Exiting...");
                        System.exit(0);
                    }

                    default ->
                            System.out.println("Unknown command. Available commands: start, shutdown, exec <cmd>, log, ex");
                }
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
