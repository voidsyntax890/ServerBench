package com.serverbench.engine.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final boolean requestLoggingEnabled;

    public ClientHandler(
            Socket clientSocket,
            boolean requestLoggingEnabled
    ) {
        this.clientSocket = clientSocket;
        this.requestLoggingEnabled = requestLoggingEnabled;
    }

    @Override
    public void run() {

        try (
                Socket socket = clientSocket;

                BufferedReader fromClient =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()
                                )
                        );

                PrintWriter toClient =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true
                        )
        ) {

            String request;

            /*
             * Keep the connection alive and process multiple
             * requests on the same TCP connection.
             *
             * This prevents the benchmark load generator from
             * exhausting local ephemeral TCP ports by creating
             * a new connection for every request.
             */
            while ((request = fromClient.readLine()) != null) {

                if (requestLoggingEnabled) {

                    System.out.println(
                            "Request received from "
                                    + socket.getRemoteSocketAddress()
                                    + ": "
                                    + request
                    );
                }

                toClient.println(
                        "Hello from ServerBench"
                );
            }

        } catch (IOException e) {

            if (requestLoggingEnabled) {

                System.err.println(
                        "Error handling client "
                                + clientSocket.getRemoteSocketAddress()
                );

                e.printStackTrace();
            }
        }
    }
}