package ch.heig.dai.lab.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.UUID;
import java.net.InetSocketAddress;
import com.google.gson.Gson;

import static java.nio.charset.StandardCharsets.*;

class Musician {
    final static String IPADDRESS = "239.255.22.5";
    final static int PORT = 9904;

    final static HashMap<String, String> instrumentSounds = new HashMap<>();

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {

            instrumentSounds.put("piano", "ti-ta-ti");
            instrumentSounds.put("trumpet", "pouet");
            instrumentSounds.put("flute", "trulu");
            instrumentSounds.put("violin", "gzi-gzi");
            instrumentSounds.put("drum", "boum-boum");

            UUID uuid = UUID.randomUUID();
            String message = uuid.toString();

            Gson gson = new Gson();
            String json = gson.toJson(this);

            byte[] payload = json.getBytes(UTF_8);
            InetSocketAddress dest_address = new InetSocketAddress(IPADDRESS, PORT);
            var packet = new DatagramPacket(payload, payload.length, dest_address);
            socket.send(packet);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}