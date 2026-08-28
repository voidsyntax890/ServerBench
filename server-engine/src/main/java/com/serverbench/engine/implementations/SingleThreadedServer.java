package com.serverbench.engine.implementations;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.serverbench.engine.core.ClientHandler;
import com.serverbench.engine.core.ServerConfig;
import com.serverbench.engine.core.ServerEngine;

public class SingleThreadedServer implements ServerEngine {

    private final ServerConfig config;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public SingleThreadedServer(ServerConfig config) {
        this.config = config;
    }

    @Override
    public String getServerType() {
        return "Single-Threaded";
    }

    @Override
    public void start() throws IOException {
        serverSocket = new ServerSocket(config.getPort());
        running = true;

        System.out.println(
                "Single-Threaded Server is running on port "
                + config.getPort()
        );

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();

                // Handle the client in the same server thread
                new ClientHandler(
                        clientSocket,
                        config.isRequestLoggingEnabled()
                ).run();

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
            System.err.println("Error while stopping Single-Threaded Server");
            e.printStackTrace();
        }
    }
}
