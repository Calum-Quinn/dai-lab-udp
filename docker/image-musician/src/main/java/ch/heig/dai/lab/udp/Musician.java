package ch.heig.dai.lab.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.net.InetSocketAddress;
import com.google.gson.Gson;

import static java.nio.charset.StandardCharsets.*;

class Musician {
    final static String IPADDRESS = "239.255.22.5";
    final static int PORT = 9904;

    final static Random random = new Random();

    final static HashMap<String, String> instrumentSounds = new HashMap<>();
    static ArrayList<String> keysList;

    private final UUID uuid;

    // Marked as transient so as not to serialize this when converting to JSON
    private final transient String instrument;

    private final String sound;

    public Musician(String instrument) {
        this.uuid = UUID.randomUUID();
        this.instrument = instrument;
        sound = instrumentSounds.get(instrument);
    }

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {

            // Fill hashmap
            instrumentSounds.put("piano", "ti-ta-ti");
            instrumentSounds.put("trumpet", "pouet");
            instrumentSounds.put("flute", "trulu");
            instrumentSounds.put("violin", "gzi-gzi");
            instrumentSounds.put("drum", "boum-boum");

            // Store key list for choosing a random instrument
            keysList = new ArrayList<>(instrumentSounds.keySet());

            // Create a musician and assign a random instrument
            Musician musician = new Musician(keysList.get(random.nextInt(0, keysList.size())));

            // Convert the musician to JSON format and send to multicast address
            Gson gson = new Gson();
            String json = gson.toJson(musician);
            byte[] payload = json.getBytes(UTF_8);
            InetSocketAddress dest_address = new InetSocketAddress(IPADDRESS, PORT);
            var packet = new DatagramPacket(payload, payload.length, dest_address);
            socket.send(packet);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}