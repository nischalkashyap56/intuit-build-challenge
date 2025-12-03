package com.producerconsumer.model;

import java.time.LocalDateTime;

// Data packet to just show the output better
public record DataPacket(long id, LocalDateTime timestamp, String payload) {
    @Override
    public String toString() {
        return "Packet[id=" + id + ", time=" + timestamp + "]";
    }
}
