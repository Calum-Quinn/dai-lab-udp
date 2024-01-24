package ch.heig.dai.lab.udp;

import java.util.UUID;

class Musician {
    private final UUID uuid;
    private final String instrument;
    private final long lastActivity;

    public long getLastActivity() {
        return lastActivity;
    }

    public Musician(String instrument, long lastActivity, UUID uuid) {
        this.uuid = uuid;
        this.instrument = instrument;
        this.lastActivity = lastActivity;
    }
}