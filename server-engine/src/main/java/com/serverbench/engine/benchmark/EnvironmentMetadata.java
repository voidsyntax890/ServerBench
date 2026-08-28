package com.serverbench.engine.benchmark;

public class EnvironmentMetadata {

    private final String operatingSystem;
    private final String javaVersion;
    private final String javaRuntime;
    private final String processor;
    private final int availableProcessors;
    private final long maxMemoryMb;

    public EnvironmentMetadata(
            String operatingSystem,
            String javaVersion,
            String javaRuntime,
            String processor,
            int availableProcessors,
            long maxMemoryMb
    ) {
        this.operatingSystem = operatingSystem;
        this.javaVersion = javaVersion;
        this.javaRuntime = javaRuntime;
        this.processor = processor;
        this.availableProcessors = availableProcessors;
        this.maxMemoryMb = maxMemoryMb;
    }

    public static EnvironmentMetadata capture() {

        return new EnvironmentMetadata(
                System.getProperty("os.name"),
                System.getProperty("java.version"),
                System.getProperty("java.runtime.version"),
                System.getProperty("os.arch"),
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().maxMemory()
                        / (1024 * 1024)
        );
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaRuntime() {
        return javaRuntime;
    }

    public String getProcessor() {
        return processor;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public long getMaxMemoryMb() {
        return maxMemoryMb;
    }

    @Override
    public String toString() {

        return "EnvironmentMetadata{" +
                "operatingSystem='" + operatingSystem + '\'' +
                ", javaVersion='" + javaVersion + '\'' +
                ", javaRuntime='" + javaRuntime + '\'' +
                ", processor='" + processor + '\'' +
                ", availableProcessors=" + availableProcessors +
                ", maxMemoryMb=" + maxMemoryMb +
                '}';
    }
}