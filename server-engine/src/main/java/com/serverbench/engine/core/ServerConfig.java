package com.serverbench.engine.core;

public class ServerConfig {

    private final int port;
    private final int threadPoolSize;
    private final boolean requestLoggingEnabled;

    public ServerConfig(
            int port,
            int threadPoolSize,
            boolean requestLoggingEnabled
    ) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.requestLoggingEnabled = requestLoggingEnabled;
    }

    public int getPort() {
        return port;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public boolean isRequestLoggingEnabled() {
        return requestLoggingEnabled;
    }
}