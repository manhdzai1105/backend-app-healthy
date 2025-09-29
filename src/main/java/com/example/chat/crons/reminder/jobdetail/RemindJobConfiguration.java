package com.example.chat.crons.reminder.jobdetail;

import com.example.chat.crons.reminder.job.RemindJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RemindJobConfiguration {
    @Bean(name="remindJobDetail")
    public JobDetail remindJobDetail(){
        return JobBuilder.newJob()
                .ofType(RemindJob.class)
                .storeDurably()
                .withIdentity("remindJob")
                .withDescription("Remind Job")
                .usingJobData("jobName", "remindJob")
                .build();
    }
}
