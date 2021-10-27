package io.kensu.collector.config;

import java.util.*;
import java.util.stream.Collectors;

public class DamProcessEnvironment {

//
//    public String damIngestionUrl(){
//        String serverHost = getOptEnv("DAM_INGESTION_URL", null);
//        if (serverHost == null) {
//            throw new RuntimeException("DAM_INGESTION_URL env var / java prop required when DAM is in online mode, e.g. https://localhost");
//        }
//        return serverHost;
//    }

    public String damIngestionToken(){
        String authToken = getOptEnv("DAM_AUTH_TOKEN", null);
        if (authToken == null) {
            throw new RuntimeException("DAM_AUTH_TOKEN env var / java prop required when DAM is in online mode");
        }
        return authToken;
    }

    public String getProcessName(){
        String serverProcessName = getOptEnv("DAM_PROCESS_NAME", "unknown process");
        String fullProcessName = String.format("%s", serverProcessName);
        return fullProcessName;
    }

    public String getProcessLauncherUserRef(){
        String userName = getOptEnv("DAM_USER_NAME", "");
        return userName;
    }

    public Map<String, String> getKensuAnnotations() {
        Map<String, String> m = new HashMap<>();
        m.put("KENSU_PROCESS_NAME", this.getProcessName());
        m.put("KENSU_LAUCHED_BY_USER", this.getProcessLauncherUserRef());
        m.put("KENSU_RUN_ENVIRONMENT", this.getRunEnvironment());
        m.put("KENSU_PROJECTS", this.getDamProjectRefs());
        m.put("KENSU_CODEBASE_LOCATION", getOptEnv("DAM_CODEBASE_LOCATION", ""));
        m.put("KENSU_CODE_VERSION", getOptEnv("DAM_CODE_VERSION", ""));
        return m;
    }

    public String getRunEnvironment(){
        return getOptEnv("DAM_RUN_ENVIRONMENT", "");
    }

    public String getDamProjectRefs(){
        String projectsStr = getOptEnv("DAM_PROJECTS", "");
        return projectsStr;
//        if (projectsStr != null){
//            List<String> projects = Arrays.stream(projectsStr.trim()
//                                            .split(";")).map(String::trim)
//                                            .filter(e->e.length()>0)
//                                            .collect(Collectors.toList());
//            if (projects.size() == 0) return null;
//            return projects;
//        } else {
//            return null;
//        }
    }

    protected String getOptEnv(String envName, String defaultValue){
        String result = System.getProperty(envName, System.getenv(envName));
        return (result == null) ? defaultValue : result;
    }
}
