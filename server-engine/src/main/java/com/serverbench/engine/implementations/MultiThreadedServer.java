package com.serverbench.engine.implementations;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.serverbench.engine.core.ClientHandler;
import com.serverbench.engine.core.ServerConfig;
import com.serverbench.engine.core.ServerEngine;

public class MultiThreadedServer implements ServerEngine {

    private final ServerConfig config;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public MultiThreadedServer(ServerConfig config) {
        this.config = config;
    }

    @Override
    public String getServerType() {
        return "Multi-Threaded";
    }

    @Override
    public void start() throws IOException {
        serverSocket = new ServerSocket(config.getPort());
        running = true;

        System.out.println(
                "Multi-Threaded Server is running on port "
                + config.getPort()
        );

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();

                Thread clientThread
                        = new Thread(
                                new ClientHandler(
                                        clientSocket,
                                        config.isRequestLoggingEnabled()
                                )
                        );

                clientThread.start();

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
            System.err.println("Error while stopping Multi-Threaded Server");
            e.printStackTrace();
        }
    }
}
