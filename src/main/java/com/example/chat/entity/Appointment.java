package com.example.chat.entity;

import com.example.chat.enums.AppointmentStatus;
import com.example.chat.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table(
        name = "appointments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_doctor_date_time",
                        columnNames = {"doctor_id", "appointment_date", "appointment_time"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK tới accounts (user bệnh nhân)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    // FK tới accounts (bác sĩ)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnore
    private Account doctor;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(name= "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "fee", nullable = false)
    @Builder.Default
    private Long fee = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    @JsonIgnore
    private Transaction transaction;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
