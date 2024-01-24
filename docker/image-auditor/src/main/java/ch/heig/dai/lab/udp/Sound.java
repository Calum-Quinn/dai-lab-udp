package ch.heig.dai.lab.udp;

import java.util.UUID;

class Sound {
    private UUID uuid;
    private long lastActivity;
    private transient String sound;

    public String getSound() {
        return sound;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public UUID getUuid() {
        return uuid;
    }
}
