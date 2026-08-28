package com.serverbench.engine.implementations;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.serverbench.engine.core.ClientHandler;
import com.serverbench.engine.core.ServerConfig;
import com.serverbench.engine.core.ServerEngine;

public class VirtualThreadServer implements ServerEngine {

    private final ServerConfig config;

    private ServerSocket serverSocket;
    private volatile boolean running;

    public VirtualThreadServer(ServerConfig config) {
        this.config = config;
    }

    @Override
    public String getServerType() {
        return "Virtual Thread";
    }

    @Override
    public void start() throws IOException {

        serverSocket = new ServerSocket(config.getPort());
        running = true;

        System.out.println(
                "Virtual Thread Server is running on port "
                + config.getPort()
        );

        while (running) {

            try {

                Socket clientSocket = serverSocket.accept();

                Thread.startVirtualThread(
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

            if (serverSocket != null
                    && !serverSocket.isClosed()) {

                serverSocket.close();
            }

        } catch (IOException e) {

            System.err.println(
                    "Error while stopping Virtual Thread Server"
            );

            e.printStackTrace();
        }
    }
}
