package com.serverbench.engine.benchmark;

import java.io.IOException;
import java.net.Socket;

import com.serverbench.engine.core.ServerEngine;

public class ComparisonRunner {

    private final BenchmarkConfig benchmarkConfig;

    public ComparisonRunner(
            BenchmarkConfig benchmarkConfig
    ) {
        this.benchmarkConfig = benchmarkConfig;
    }

    public BenchmarkResult runComparison(
            ServerEngine server
    ) {

        System.out.println(
                "\n========================================"
        );

        System.out.println(
                "Starting benchmark for: "
                        + server.getServerType()
        );

        System.out.println(
                "========================================"
        );

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();

        waitForServer();

        BenchmarkRunner benchmarkRunner =
                new BenchmarkRunner(
                        benchmarkConfig,
                        server.getServerType()
                );

        BenchmarkResult result =
                benchmarkRunner.run();

        server.stop();

        try {
            serverThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    private void waitForServer() {

        System.out.println(
                "Waiting for server to start..."
        );

        while (true) {

            try (
                    Socket socket = new Socket(
                            benchmarkConfig.getHost(),
                            benchmarkConfig.getPort()
                    )
            ) {

                System.out.println(
                        "Server is ready."
                );

                return;

            } catch (IOException e) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}