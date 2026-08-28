package com.serverbench.engine.implementations;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.serverbench.engine.core.ClientHandler;
import com.serverbench.engine.core.ServerConfig;
import com.serverbench.engine.core.ServerEngine;

public class ThreadPoolServer implements ServerEngine {

    private final ServerConfig config;
    private final ExecutorService threadPool;

    private ServerSocket serverSocket;
    private volatile boolean running;

    public ThreadPoolServer(ServerConfig config) {
        this.config = config;
        this.threadPool = Executors.newFixedThreadPool(config.getThreadPoolSize());
    }

    @Override
    public String getServerType() {
        return "Thread Pool";
    }

    @Override
    public void start() throws IOException {

        serverSocket = new ServerSocket(config.getPort());
        running = true;

        System.out.println(
                "Thread Pool Server is running on port "
                + config.getPort()
                + " with pool size "
                + config.getThreadPoolSize()
        );

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();

                threadPool.execute(
                        new ClientHandler(
                                clientSocket,
                                config.isRequestLoggingEnabled()
                        )
                );

            } catch (IOException e) {
                if (running) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void stop() {

        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error while stopping Thread Pool Server");
            e.printStackTrace();
        }

        threadPool.shutdown();
    }
}
