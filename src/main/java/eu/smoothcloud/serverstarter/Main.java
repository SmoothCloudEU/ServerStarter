package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static final ServiceRegistry registry = new ServiceRegistry();

    public static void main(String[] args) {
        Server server = new Server(
                UUID.randomUUID(),
                "Lobby-1",
                "Lobby",
                "",
                8084,
                "",
                "server.jar",
                10,
                "",
                1,
                1,
                95,
                128,
                1024,
                false,
                false
        );

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

            switch (command.toLowerCase()) {
                case "start":
                    registry.registerService(server);
                    break;
                case "shutdown":
                    registry.unregisterService(server.getUniqueId());
                    break;
                case "ex":
                    registry.unregisterService(server.getUniqueId());
                    System.exit(0);
                    break;
                default:
                    System.out.println("Unknown command");
            }

        }

    }
}

