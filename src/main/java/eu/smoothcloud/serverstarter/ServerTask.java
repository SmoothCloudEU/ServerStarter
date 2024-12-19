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
        if (this.serverMap.containsKey(uniqueId)) {
            return;
        }
        if (this.processMap.containsKey(uniqueId)) {
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

        Future<?> future = this.executorService.submit(() -> {
            try {
                Process process = processBuilder.start();
                this.serverMap.put(uniqueId, server);
                this.processMap.put(uniqueId, process);
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.fillInStackTrace();
            } finally {
                this.processMap.remove(uniqueId);
            }
        });
        this.serverIdFutureMap.put(uniqueId, future);
    }

    void stop(UUID uniqueId) {
        Future<?> future = this.serverIdFutureMap.get(uniqueId);
        if (future == null) {
            return;
        }
        future.cancel(true);
        Process process = this.processMap.get(uniqueId);
        if (process != null && process.isAlive()) {
            try {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    if (this.serverMap.get(uniqueId).isProxy()) {
                        this.write(writer, "end\n");
                        return;
                    }
                    this.write(writer, "stop\n");
                }
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.processMap.remove(uniqueId);
        this.serverIdFutureMap.remove(uniqueId);
    }
    
    private void write(BufferedWriter writer, String message) throws IOException {
        writer.write(message);
        writer.flush();
    }
}
