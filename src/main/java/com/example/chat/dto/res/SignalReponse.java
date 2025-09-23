package com.example.chat.dto.res;

import lombok.Data;

@Data
public class SignalReponse {
    private String type;      // offer, answer, iceCandidate, mute, videoOff, etc.
    private Object data;
    private Long senderId;    // người gửi

}
