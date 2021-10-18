package io.kensu.collector.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DamProcessEnvironment {

    public boolean isOffline() {
        boolean isOffline = getOptEnv("DAM_OFFLINE", "false").toLowerCase().equals("true");
        // isOffline = true;
        return isOffline;
    }

    public String damIngestionUrl(){
        String serverHost = getOptEnv("DAM_INGESTION_URL", null);
        if (serverHost == null) {
            throw new RuntimeException("DAM_INGESTION_URL env var / java prop required when DAM is in online mode, e.g. https://localhost");
        }
        return serverHost;
    }

    public String damIngestionToken(){
        String authToken = getOptEnv("DAM_AUTH_TOKEN", null);
        if (authToken == null) {
            throw new RuntimeException("DAM_AUTH_TOKEN env var / java prop required when DAM is in online mode");
        }
        return authToken;
    }

    public String enqueProcess(){
        String serverProcessName = getOptEnv("DAM_PROCESS_NAME", "unknown process");
        String fullProcessName = String.format("%s", serverProcessName);
        return fullProcessName;
    }

    public String getProcessLauncherUserRef(){
        String userName = getOptEnv("DAM_USER_NAME", null);
        return userName;
    }

    public String getRunEnvironment(){
        return getOptEnv("DAM_RUN_ENVIRONMENT", null);
    }

    public List<String> getDamProjectRefs(){
        String projectsStr = getOptEnv("DAM_PROJECTS", null);
        if (projectsStr != null){
            List<String> projects = Arrays.stream(projectsStr.trim()
                                            .split(";")).map(String::trim)
                                            .filter(e->e.length()>0)
                                            .collect(Collectors.toList());
            if (projects.size() == 0) return null;
            return projects;
        } else {
            return null;
        }
    }

    public String getExecutedCodeVersionRef() {
        String codeBaseLocation = getOptEnv("DAM_CODEBASE_LOCATION", null);
        String codeVersionValue = getOptEnv("DAM_CODE_VERSION", null);
        if (codeBaseLocation != null && codeVersionValue != null) {
            // FIXME
            return null;
        }
        return null;
    }

//    public ProcessRun enqueProcessRun(Process process,
//                                         String endpointName,
//                                         DamBatchBuilder batchBuilder) {
//        String processName = process.getPk().getQualifiedName();
//        ProcessRun processRun =  new ProcessRun()
//                .pk(new ProcessRunPK()
//                        .qualifiedName(String.format("%s :: %s run at %d", processName, endpointName, System.currentTimeMillis()))
//                        .processRef(new ProcessRef().byPK(process.getPk())))
//                .launchedByUserRef(getProcessLauncherUserRef(batchBuilder))
//                .environment(getRunEnvironment())
//                .projectsRefs(getDamProjectRefs(batchBuilder))
//                .executedCodeVersionRef(getExecutedCodeVersionRef(batchBuilder));
//        batchBuilder.add(processRun);
//        return processRun;
//    }

    protected String getOptEnv(String envName, String defaultValue){
        String result = System.getProperty(envName, System.getenv(envName));
        return (result == null) ? defaultValue : result;
    }
}
