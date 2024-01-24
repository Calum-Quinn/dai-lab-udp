package ch.heig.dai.lab.udp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import static java.nio.charset.StandardCharsets.*;


public class Auditor {
    final static String IPADDRESS = "239.255.22.5";
    final static int UDP_PORT = 9904;
    final static int TCP_PORT = 2205;
    final static HashMap<String, String> instrumentSounds = new HashMap<>();
    final static ArrayList<Musician> musicians = new ArrayList<>();

    // Records so as not to use classes for purely temporary objects
    public record Musician(UUID uuid, String instrument, long lastActivity) {}
    public record Sound(UUID uuid, String sound, long lastActivity) {}

    public static void main(String[] args) {

        // Fill the hashmap
        instrumentSounds.put("ti-ta-ti", "piano");
        instrumentSounds.put("pouet", "trumpet");
        instrumentSounds.put("trulu", "flute");
        instrumentSounds.put("gzi-gzi", "violin");
        instrumentSounds.put("boum-boum", "drum");

        // Start the two threads for receiving UDP and for sending/receiving TCP
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(Auditor::listenForUdp);
            executor.submit(Auditor::listenForTcp);
        }
    }

    private static void listenForTcp() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            // While true so that it continues to receive requests indefinitely
            while (true) {
                try (Socket socket = serverSocket.accept();
                     var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8))){

                    // Remove inactive musicians
                    musicians.removeIf(musician -> musician.lastActivity() < System.currentTimeMillis() - 5000);

                    // Initialize serializer and send list of musicians in JSON format
                    Gson gson = new Gson();
                    String json = gson.toJson(musicians);
                    out.write(json);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void listenForUdp() {

        try (MulticastSocket socket = new MulticastSocket(UDP_PORT)) {
            // Initialize the multicast address
            InetSocketAddress group_address = new InetSocketAddress(IPADDRESS, UDP_PORT);
            NetworkInterface netif = NetworkInterface.getByName("eth0");
            socket.joinGroup(group_address, netif);

            // Try group so that it leaves the multicast group in any case
            try {
                // While true so that it continues to receive sounds indefinitely
                while (true) {

                    // Receive the next message and convert it to a string
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength(), UTF_8);

                    // Deserialize the message
                    Gson gson = new Gson();
                    Sound sound = gson.fromJson(message, Sound.class);

                    // Create a musician with only the required attributes from the Sound
                    Musician musician = new Musician(sound.uuid(), instrumentSounds.get(sound.sound()), sound.lastActivity());

                    // Remove previous versions of the same musician
                    musicians.removeIf(existingMusician -> existingMusician.uuid().equals(musician.uuid()));
                    musicians.add(musician);

                    System.out.println("Received message: " + message);
                }
            } finally {
                // Ensure that leaveGroup is executed, even if an exception occurs
                socket.leaveGroup(group_address, netif);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
