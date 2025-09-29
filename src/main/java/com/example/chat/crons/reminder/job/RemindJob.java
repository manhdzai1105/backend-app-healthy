package com.example.chat.crons.reminder.job;

import com.example.chat.crons.reminder.service.RemindService;
import com.example.chat.entity.Appointment;
import com.example.chat.enums.AppointmentStatus;
import com.example.chat.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemindJob implements Job {
    private final AppointmentRepository appointmentRepository;
    private final RemindService remindService;

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final List<LocalTime> FIXED_SLOTS = Arrays.asList(
            LocalTime.of(9, 0),
            LocalTime.of(9, 30),
            LocalTime.of(10, 0),
            LocalTime.of(10, 30),
            LocalTime.of(11, 0),
            LocalTime.of(11, 30),
            LocalTime.of(14, 0),
            LocalTime.of(14, 30),
            LocalTime.of(15, 0),
            LocalTime.of(15, 30),
            LocalTime.of(16, 0),
            LocalTime.of(16, 30)
    );

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("------------ RemindJob started ------------");

        try {
            ZonedDateTime now = ZonedDateTime.now(ZONE).withSecond(0).withNano(0);
            LocalDate today = now.toLocalDate();

            LocalTime slot = now.toLocalTime().plusMinutes(15).withSecond(0).withNano(0);

            if (!FIXED_SLOTS.contains(slot)) {
                log.debug("Không có slot hợp lệ tại {}", slot);
                return;
            }

            List<Appointment> appts = appointmentRepository
                    .findByStatusAndAppointmentDateAndAppointmentTime(
                            AppointmentStatus.CONFIRMED, today, slot
                    );

            appts.forEach(remindService::send15mReminder);
            log.info("Nhắc {} appointment slot {} tại {}", appts.size(), slot, now);

        } catch (Exception e) {
            log.error("Error processing RemindJob: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        } finally {
            log.info("------------ RemindJob finished ------------");
        }
    }
}
