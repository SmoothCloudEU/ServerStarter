package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static final ServiceRegistry registry = new ServiceRegistry();

    public static void main(String[] args) {
        new Thread() {
            public void run() {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (true) {
                        System.out.print("serverstarter » ");

                        String input = scanner.nextLine().trim();

                        // Check for empty input
                        if (input.isEmpty()) {
                            System.out.println("Please enter a valid command.");
                            continue;
                        }


                        String[] inputParts = input.split(" ");
                        String command = inputParts[0].toLowerCase();
                        String[] arguments = Arrays.copyOfRange(inputParts, 1, inputParts.length);

                        switch (command) {
                            case "start" -> {
                                Server server = new Server(UUID.randomUUID(), "Lobby-1", "Lobby", "", 8084, "", "server.jar", 10, "", 1, 1, 95, 128, 1024, false, false);
                                registry.registerService(server);
                            }
                            case "shutdown" -> {
                                try {
                                    if (registry.getService(arguments[0]) != null) {
                                        registry.unregisterService(registry.getService(arguments[0]).getUniqueId());
                                        continue;
                                    }
                                    System.out.println("Server is not running.");
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    System.out.println("Usage: shutdown <service>");
                                }
                            }
                            case "exec" -> {
                                try {
                                    if (registry.getService(arguments[0]) != null) {
                                        if (Arrays.stream(arguments).toArray().length <= 1) {
                                            System.out.println("Please provide a command to execute.");
                                            continue;
                                        }
                                        String[] arguments2 = Arrays.copyOfRange(inputParts, 2, inputParts.length);
                                        String argumentsAsString = String.join(" ", arguments2);
                                        registry.execute(registry.getService(arguments[0]).getUniqueId(), argumentsAsString);
                                        continue;
                                    }
                                    System.out.println("Server is not running.");
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    System.out.println("Usage: exec <service> <command>");
                                }
                            }
                            case "log" -> {
                                try {
                                    if (registry.getService(arguments[0]) != null) {
                                        registry.showLogs(registry.getService(arguments[0]).getUniqueId());
                                        continue;
                                    }
                                    System.out.println("Server is not running.");
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    System.out.println("Usage: log <service>");
                                }
                            }
                            case "ex" -> {
                                registry.getServices().forEach((uuid, server) -> {
                                    registry.unregisterService(uuid);
                                });
                                System.out.println("Exiting...");
                                System.exit(0);
                            }

                            default -> System.out.println("""
                                    Unknown command.
                                    Available commands:
                                    - start <service>
                                    - log <service>
                                    - exec <service> <command>
                                    - shutdown <service>
                                    - ex""");
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting down...");
                registry.getServices().forEach((uuid, service) -> {
                    registry.unregisterService(uuid);
                });
                System.exit(0);
            }
        });
    }
}
