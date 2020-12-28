package io.kensu.collector;

import io.kensu.collector.model.DamBatchBuilder;
import io.kensu.dam.model.*;
import io.kensu.dam.model.Process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DamProcessEnvironment {

    public boolean isOffline() {
        boolean isOffline = getOptEnv("DAM_OFFLINE", "").toLowerCase().equals("true");
        // isOffline = true;
        return isOffline;
    }

    public String damIngestionUrl(){
        String serverHost = System.getenv("DAM_INGESTION_URL");
        if (serverHost == null) {
            throw new RuntimeException("DAM_INGESTION_URL env var required when DAM is in online mode, e.g. https://localhost");
        }
        return serverHost;
    }

    public String damIngestionToken(){
        String authToken = System.getenv("DAM_AUTH_TOKEN");
        if (authToken == null) {
            throw new RuntimeException("DAM_AUTH_TOKEN env var required when DAM is in online mode");
        }
        return authToken;
    }

    public Process enqueProcess(DamBatchBuilder batchBuilder){
        String serverProcessName = getOptEnv("DAM_PROCESS_NAME", "unknown process");
        String fullProcessName = String.format("%s", serverProcessName);
        Process process = new Process().pk(new ProcessPK().qualifiedName(fullProcessName));
        batchBuilder.add(process);
        return process;
    }

    public UserRef getProcessLauncherUserRef(DamBatchBuilder batchBuilder){
        String userName = getOptEnv("DAM_USER_NAME", null);
        if (userName != null){
            User user = new User().pk(new UserPK().name(userName));
            batchBuilder.add(user);
            return new UserRef().byPK(user.getPk());
        }
        return null;
    }

    public String getRunEnvironment(){
        return getOptEnv("DAM_RUN_ENVIRONMENT", null);
    }

    public List<ProjectRef> getDamProjectRefs(DamBatchBuilder batchBuilder){
        String projectsStr = getOptEnv("DAM_PROJECTS", null);
        if (projectsStr != null){
            List<String> projects = Arrays.asList(projectsStr.trim().split(";"));
            if (projects.size() == 0) return null;
            List<ProjectRef> projectRefs = new ArrayList<>();
            for (String projectName: projectsStr.split(";")){
                Project project = new Project().pk(new ProjectPK().name(projectName));
                batchBuilder.add(project);
                projectRefs.add(new ProjectRef().byPK(project.getPk()));
            }
            return projectRefs;
        } else {
            return null;
        }
    }

    public CodeVersionRef getExecutedCodeVersionRef(DamBatchBuilder batchBuilder) {
        String codeBaseLocation = getOptEnv("DAM_CODEBASE_LOCATION", null);
        String codeVersionValue = getOptEnv("DAM_CODE_VERSION", null);
        if (codeBaseLocation != null && codeVersionValue != null) {
            CodeBase codeBase = new CodeBase().pk(new CodeBasePK().location(codeBaseLocation));
            CodeVersionPK codeVersionPK = new CodeVersionPK()
                    .codebaseRef(new CodeBaseRef().byPK(codeBase.getPk()))
                    .version(codeVersionValue);
            CodeVersion codeVersion = new CodeVersion().pk(codeVersionPK);
            batchBuilder.add(codeBase);
            batchBuilder.add(codeVersion);
            return new CodeVersionRef().byPK(codeVersionPK);
        }
        return null;
    }

    public ProcessRun enqueProcessRun(Process process,
                                         String endpointName,
                                         DamBatchBuilder batchBuilder) {
        String processName = process.getPk().getQualifiedName();
        ProcessRun processRun =  new ProcessRun()
                .pk(new ProcessRunPK()
                        .qualifiedName(String.format("%s :: %s run at %d", processName, endpointName, System.currentTimeMillis()))
                        .processRef(new ProcessRef().byPK(process.getPk())))
                .launchedByUserRef(getProcessLauncherUserRef(batchBuilder))
                .environment(getRunEnvironment())
                .projectsRefs(getDamProjectRefs(batchBuilder))
                .executedCodeVersionRef(getExecutedCodeVersionRef(batchBuilder));
        batchBuilder.add(processRun);
        return processRun;
    }

    protected String getOptEnv(String envName, String defaultValue){
        String result = System.getenv(envName);
        return (result == null) ? defaultValue : result;
    }
}
