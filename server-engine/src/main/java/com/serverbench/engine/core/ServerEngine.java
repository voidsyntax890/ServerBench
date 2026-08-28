package com.serverbench.engine.core;

import java.io.IOException;

public interface ServerEngine {

    String getServerType();

    void start() throws IOException;

    void stop();
}