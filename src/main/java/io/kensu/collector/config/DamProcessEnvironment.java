package io.kensu.collector.config;

import java.util.*;

public class DamProcessEnvironment {

    Properties properties;

    public DamProcessEnvironment(Properties properties) {
        this.properties = properties;
    }

    public String getKensuIngestionToken() {
        String authToken = getOptEnvOrProp("KENSU_AUTH_TOKEN", "kensu.collector.api.token", null);
        if (authToken == null) {
            throw new RuntimeException("KENSU_AUTH_TOKEN env var or kensu.collector.api.token java property must be provided");
        }
        return authToken;
    }

    public String getKensuIngestionUrl() {
        String authToken = getOptEnvOrProp("KENSU_ZIPKIN_ENDPOINT", "kensu.collector.api.url", null);
        if (authToken == null) {
            throw new RuntimeException("KENSU_ZIPKIN_ENDPOINT env var or kensu.collector.api.url java property must be provided");
        }
        return authToken;
    }

    public String getProcessName() {
        String serverProcessName = getOptEnvOrProp("KENSU_PROCESS_NAME", "app.artifactId", "unknown process");
        String fullProcessName = String.format("%s", serverProcessName);
        return fullProcessName;
    }

    public String getProcessLauncherUserRef() {
        String userName = getOptEnvOrProp("KENSU_USER_NAME", "kensu.collector.run.user", System.getenv("USER"));
        return userName;
    }

    public Map<String, String> getKensuAnnotations() {
        Map<String, String> m = new HashMap<>();
        m.put("KENSU_PROCESS_NAME", this.getProcessName());
        m.put("KENSU_LAUCHED_BY_USER", this.getProcessLauncherUserRef());
        m.put("KENSU_RUN_ENVIRONMENT", this.getRunEnvironment());
        m.put("KENSU_PROJECTS", this.getDamProjectRefs());
        m.put("KENSU_CODEBASE_LOCATION", getCodebaseLocation());
        m.put("KENSU_CODE_VERSION", getCodeVersion());
        return m;
    }

    public String getCodebaseLocation() {
        return getOptEnvOrProp("KENSU_CODEBASE_LOCATION", "git.remote.origin.url", "");
    }

    public String getCodeVersion() {
        return getOptEnvOrDefault("KENSU_CODE_VERSION", this.properties.getProperty("app.version") + "_" + this.properties.getProperty("git.commit.id.describe-short"));
    }

    public String getRunEnvironment() {
        return getOptEnvOrProp("KENSU_RUN_ENVIRONMENT", "kensu.collector.run.env", "unknown");
    }

    public String getDamProjectRefs() {
        return getOptEnvOrProp("DAM_PROJECTS", "kensu.collector.run.projects", "");
    }

    public String getOptEnvOrProp(String envName, String propName, String defaultValue) {
        String result = System.getenv(envName);
        // we probably want environment variables to override the properties
        if (result == null) {
            return this.properties.getProperty(propName, defaultValue);
        }
        return result;
    }

    public String getOptEnvOrDefault(String envName, String defaultValue) {
        String result = System.getenv(envName);
        // we probably want environment variables to override the properties
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

}
