package com.example.chat.dto.req;

import com.example.chat.enums.AppointmentStatus;
import lombok.Data;

@Data
public class UpdateAppointmentStatusRequest {
    private AppointmentStatus status;
}
