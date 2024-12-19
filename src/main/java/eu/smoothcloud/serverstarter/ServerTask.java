package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.*;

public class ServerTask {

    private final ConcurrentHashMap<UUID, Server> serverMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Future<?>> serverIdFutureMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Process> processMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, StringBuilder> processLogs = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Starts a server process.
     *
     * @param uniqueId The unique ID of the server.
     * @param server   The server instance.
     */
    public void start(UUID uniqueId, Server server) {
        if (serverMap.containsKey(uniqueId) || processMap.containsKey(uniqueId)) {
            return;
        }

        String command = buildCommand(server);
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processLogs.put(uniqueId, new StringBuilder());

        Future<?> future = executorService.submit(() -> {
            try {
                Process process = processBuilder.start();
                serverMap.put(uniqueId, server);
                processMap.put(uniqueId, process);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    StringBuilder logBuilder = processLogs.get(uniqueId);

                    while ((line = reader.readLine()) != null) {
                        if (logBuilder == null) {
                            return;
                        }
                        synchronized (logBuilder) {
                            logBuilder.append("[").append(server.getName().toUpperCase()).append("]")
                                    .append(line).append(System.lineSeparator());
                        }
                    }
                }

                process.waitFor();
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                cleanupResources(uniqueId);
            }
        });

        serverIdFutureMap.put(uniqueId, future);
    }

    /**
     * Stops a server process.
     *
     * @param uniqueId The unique ID of the server.
     */
    public void stop(UUID uniqueId) {
        Future<?> future = serverIdFutureMap.get(uniqueId);
        if (future != null) {
            future.cancel(true);
        }

        Process process = processMap.get(uniqueId);
        if (process == null || !process.isAlive()) {
            System.out.println("No active process found for UUID: " + uniqueId);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            String stopCommand = serverMap.get(uniqueId).isProxy() ? "end\n" : "stop\n";
            write(writer, stopCommand);

            if (process.waitFor(10, TimeUnit.SECONDS)) {
                System.out.println("Process terminated gracefully.");
                return;
            }

            System.out.println("Process did not terminate within timeout. Forcibly terminating...");
            process.destroyForcibly();
        } catch (IOException e) {
            System.err.println("Failed to send stop command: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Stop operation interrupted. Attempting to terminate process...");
            Thread.currentThread().interrupt();
        }

        cleanupResources(uniqueId);
    }

    /**
     * Displays logs of a server process.
     *
     * @param uniqueId The unique ID of the server.
     */
    public void showLogs(UUID uniqueId) {
        StringBuilder logs = processLogs.get(uniqueId);
        if (logs == null) {
            System.out.println("No logs available for server: " + uniqueId);
            return;
        }

        System.out.println("Logs for server " + uniqueId + ":\n" + logs);
    }

    /**
     * Executes a command on the running server process.
     *
     * @param uniqueId The unique ID of the server.
     * @param command  The command to execute.
     */
    public void execute(UUID uniqueId, String command) {
        if (!serverMap.containsKey(uniqueId) || !processMap.containsKey(uniqueId) || serverIdFutureMap.get(uniqueId) == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(processMap.get(uniqueId).getOutputStream()))) {
            write(writer, command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the command string for the server process.
     *
     * @param server The server instance.
     * @return The complete command string.
     */
    private String buildCommand(Server server) {
        return (server.getJavaPath().isEmpty() ? "java" : server.getJavaPath()) +
                " -Xms" + server.getMinimumMemory() + "M" +
                " -Xmx" + server.getMaximumMemory() + "M" +
                " -XX:+UseG1GC" +
                (server.isProxy() ? "" : " -Dcom.mojang.eula.agree=true -DIReallyKnowWhatIAmDoingISwear") +
                " -jar " + server.getServerSoftware() +
                " --port " + server.getPort() +
                (server.isProxy() ? "" : " nogui");
    }

    /**
     * Writes a command to the server process.
     *
     * @param writer  The BufferedWriter to write to.
     * @param message The message to send to the server.
     * @throws IOException If an I/O error occurs.
     */
    private void write(BufferedWriter writer, String message) throws IOException {
        writer.write(message);
        writer.flush();
    }

    /**
     * Cleans up resources associated with a server.
     *
     * @param uniqueId The unique ID of the server.
     */
    private void cleanupResources(UUID uniqueId) {
        serverMap.remove(uniqueId);
        processMap.remove(uniqueId);
        serverIdFutureMap.remove(uniqueId);
        processLogs.remove(uniqueId);
    }
}
