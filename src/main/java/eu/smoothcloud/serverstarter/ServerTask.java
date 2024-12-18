package eu.smoothcloud.serverstarter;

import eu.smoothcloud.serverstarter.utils.Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.*;

public class ServerTask {

    private final ConcurrentHashMap<Server, Future<?>> serverMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Server, Process> processMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    void start(Server server) {
        if (processMap.containsKey(server)) {
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
                processMap.put(server, process);
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                processMap.remove(server);
            }
        });
        serverMap.put(server, future);
    }

    void stop(Server server) {
        Future<?> future = serverMap.get(server);
        if (future == null) {
            return;
        }
        future.cancel(true);
        Process process = processMap.get(server);
        if (process != null && process.isAlive()) {
            try {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    if (server.isProxy()) {
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
        processMap.remove(server);
        serverMap.remove(server);
    }
}
