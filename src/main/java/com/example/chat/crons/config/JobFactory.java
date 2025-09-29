package com.example.chat.crons.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JobFactory {
    private final JobConfigs jobConfigs;
    private final Map<String, String> jobMap = new HashMap<>();

    public JobFactory(JobConfigs jobConfigs) {
        this.jobConfigs = jobConfigs;
        initJobMap();
    }

    public String getCronJobByName(String name) {
        return jobMap.get(name);
    }

    private void initJobMap() {
        if (jobConfigs == null || jobConfigs.getProfiles() == null) {
            return;
        }

        for (JobProfile jobProfile : jobConfigs.getProfiles()) {
            String name = jobProfile.getName();
            if (name != null && !name.isEmpty() && jobProfile.getCron() != null) {
                String existed = jobMap.get(name);
                if (existed != null) {
                    log.warn("Job {} existed, will be override", name);
                }
                jobMap.put(name, jobProfile.getCron());
            }
        }
    }
}