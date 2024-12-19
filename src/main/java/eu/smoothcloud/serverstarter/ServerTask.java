package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;
import java.util.concurrent.*;

public class ServerTask {

    private final ConcurrentHashMap<UUID, Server> serverMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Future<?>> serverIdFutureMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Process> processMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    void start(UUID uniqueId, Server server) {
        if (serverMap.containsKey(uniqueId)) {
            return;
        }
        if (processMap.containsKey(uniqueId)) {
            return;
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
                server.getJavaPath().isEmpty() ? "java" : server.getJavaPath(),
                "-Xms" + server.getMinimumMemory() + "M",
                "-Xmx" + server.getMaximumMemory() + "M",
                "-Dcom.mojang.eula.agree=true",
                "-DIReallyKnowWhatIAmDoingISwear",
                "-jar",
                server.getServerSoftware(),
                "--port", String.valueOf(server.getPort()),
                "nogui"
        );

        Future<?> future = executorService.submit(() -> {
            try {
                Process process = processBuilder.start();
                serverMap.put(uniqueId, server);
                processMap.put(uniqueId, process);
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                serverMap.remove(uniqueId);
                processMap.remove(uniqueId);
            }
        });
        serverIdFutureMap.put(uniqueId, future);
    }

    void stop(UUID uniqueId) {
        Future<?> future = serverIdFutureMap.get(uniqueId);
        if (future == null) {
            return;
        }
        future.cancel(true);
        Process process = processMap.get(uniqueId);
        if (process != null && process.isAlive()) {
            try {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    if (serverMap.get(uniqueId).isProxy()) {
                        writer.write("end\n");
                    } else {
                        writer.write("stop\n");
                    }
                    writer.flush();
                }
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
        processMap.remove(uniqueId);
        serverIdFutureMap.remove(uniqueId);
    }
}
