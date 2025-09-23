package com.example.chat.dto;

import lombok.Data;

@Data
public class SignalPayload {
    private String type;      // offer, answer, iceCandidate, mute, videoOff, etc.
    private Object data;      // SDP, ICE, hoặc null nếu chỉ là tín hiệu

}

