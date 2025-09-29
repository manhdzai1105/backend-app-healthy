package com.example.chat.repository;

import com.example.chat.entity.Account;
import com.example.chat.entity.Appointment;
import com.example.chat.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Kiểm tra một slot có tồn tại hay không
    Optional<Appointment> findByDoctorAndAppointmentDateAndAppointmentTime(
            Account doctor, LocalDate date, LocalTime time
    );

    // Lấy tất cả giờ đã đặt của 1 bác sĩ trong 1 ngày
    @Query("SELECT a.appointmentTime FROM Appointment a " +
            "WHERE a.doctor = :doctor " +
            "AND a.appointmentDate = :date " +
            "AND a.status <> com.example.chat.enums.AppointmentStatus.CANCELLED")
    List<LocalTime> findBookedSlots(Account doctor, LocalDate date);

    List<Appointment> findByDoctor(Account doctor);
    List<Appointment> findByUser(Account user);

    List<Appointment> findByDoctorAndStatus(Account doctor, AppointmentStatus status);

    List<Appointment> findByUserAndStatus(Account user, AppointmentStatus status);

    List<Appointment> findByStatusAndAppointmentDateAndAppointmentTime(
            AppointmentStatus status,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );

}
