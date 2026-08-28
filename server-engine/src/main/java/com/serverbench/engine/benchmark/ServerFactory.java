package com.serverbench.engine.benchmark;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import com.serverbench.engine.core.ServerConfig;
import com.serverbench.engine.core.ServerEngine;
import com.serverbench.engine.implementations.MultiThreadedServer;
import com.serverbench.engine.implementations.SingleThreadedServer;
import com.serverbench.engine.implementations.ThreadPoolServer;
import com.serverbench.engine.implementations.VirtualThreadServer;

public final class ServerFactory {

    private ServerFactory() {
        // Utility class.
    }

    /**
     * Creates factories for all supported server architectures.
     *
     * Each Supplier creates a fresh ServerEngine instance.
     *
     * This is important for repeated experiments because some
     * server implementations, such as ThreadPoolServer, cannot
     * be reused after they have been stopped.
     */
    public static Map<
            ServerArchitecture,
            Supplier<ServerEngine>
            > createFactories(ServerConfig config) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "Server configuration cannot be null."
            );
        }

        Map<
                ServerArchitecture,
                Supplier<ServerEngine>
                > factories =
                new EnumMap<>(
                        ServerArchitecture.class
                );

        factories.put(
                ServerArchitecture.SINGLE_THREADED,
                () -> new SingleThreadedServer(config)
        );

        factories.put(
                ServerArchitecture.MULTI_THREADED,
                () -> new MultiThreadedServer(config)
        );

        factories.put(
                ServerArchitecture.THREAD_POOL,
                () -> new ThreadPoolServer(config)
        );

        factories.put(
                ServerArchitecture.VIRTUAL_THREAD,
                () -> new VirtualThreadServer(config)
        );

        return factories;
    }

    /**
     * Creates a factory for one specific architecture.
     *
     * The returned Supplier creates a new server instance
     * every time it is called.
     */
    public static Supplier<ServerEngine> createFactory(
            ServerArchitecture architecture,
            ServerConfig config
    ) {

        if (architecture == null) {
            throw new IllegalArgumentException(
                    "Server architecture cannot be null."
            );
        }

        if (config == null) {
            throw new IllegalArgumentException(
                    "Server configuration cannot be null."
            );
        }

        return switch (architecture) {

            case SINGLE_THREADED ->
                    () -> new SingleThreadedServer(config);

            case MULTI_THREADED ->
                    () -> new MultiThreadedServer(config);

            case THREAD_POOL ->
                    () -> new ThreadPoolServer(config);

            case VIRTUAL_THREAD ->
                    () -> new VirtualThreadServer(config);
        };
    }
}