package ch.heig.dai.lab.udp;

import java.util.UUID;

class Musician {
    private UUID uuid;
    // Marked as transient so as not to serialize this when converting to JSON
    private String instrument;
    private long lastActivity;

    private transient String sound;

    public String getSound() {
        return sound;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getInstrument() {
        return instrument;
    }

//    public Musician(String instrument, long lastActivity, UUID uuid) {
//        this.uuid = uuid;
//        this.instrument = instrument;
//        this.lastActivity = lastActivity;
//    }
}