package com.example.chat.crons.reminder.trigger;

import com.example.chat.crons.config.JobFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class TriggerRemindConfiguration {
    private final JobFactory jobFactory;

    @Bean
    public Trigger triggerAppointmentJob(@Qualifier("remindJobDetail") JobDetail remindJobDetail) {
        log.info("Creating trigger for Remind Job");
        try {
            String jobName = "remindJob";
            return TriggerBuilder.newTrigger()
                    .forJob(remindJobDetail)
                    .withIdentity(jobName + "Trigger")
                    .withDescription("Trigger for Remind Job")
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobFactory.getCronJobByName(jobName)))
                    .build();
        } catch (Exception e) {
            log.error("Error creating trigger for Remind Job: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create RemindJob trigger", e);
        }
    }
}
