package ch.heig.dai.lab.udp;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.UUID;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import static java.nio.charset.StandardCharsets.*;

class Musician {
    final static String IPADDRESS = "239.255.22.5";
    final static int PORT = 9904;
    final static HashMap<String, String> instrumentSounds = new HashMap<>();
    private final UUID uuid;
    // Marked as transient so as not to serialize this when converting to JSON
    private final transient String instrument;
    private final String sound;
    private long lastActivity;

    public Musician(String instrument) {
        this.uuid = UUID.randomUUID();
        this.instrument = instrument;
        sound = instrumentSounds.get(instrument);
        lastActivity = System.currentTimeMillis();
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public static void main(String[] args) {
        // Fill the hashmap
        instrumentSounds.put("piano", "ti-ta-ti");
        instrumentSounds.put("trumpet", "pouet");
        instrumentSounds.put("flute", "trulu");
        instrumentSounds.put("violin", "gzi-gzi");
        instrumentSounds.put("drum", "boum-boum");

        // Check that an instrument was provided for the musician
        if (args.length == 0 || !instrumentSounds.containsKey(args[0])) {
            System.out.println("Invalid instrument");
            System.exit(1);
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            // Create a new musician
            Musician musician = new Musician(args[0]);

            // Initialize the serializer
            Gson gson = new Gson();

            // Initialise the multicast address
            InetSocketAddress dest_address = new InetSocketAddress(IPADDRESS, PORT);

            // Make a sound every second
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    // Update the time of last activity before sending the next sound
                    musician.setLastActivity(System.currentTimeMillis());

                    // Convert the musician to JSON format and send to multicast address
                    String json = gson.toJson(musician);
                    byte[] payload = json.getBytes(UTF_8);
                    var packet = new DatagramPacket(payload, payload.length, dest_address);
                    socket.send(packet);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }, 0, 1, TimeUnit.SECONDS);

            // Add a delay so the thread does not close before sending the next sound
            while (true) {
                try {
                    Thread.sleep(1000); // Add a small delay to avoid busy-waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}