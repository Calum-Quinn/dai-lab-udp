package ch.heig.dai.lab.udp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static void main(String[] args) {
        instrumentSounds.put("ti-ta-ti", "piano");
        instrumentSounds.put("pouet", "trumpet");
        instrumentSounds.put("trulu", "flute");
        instrumentSounds.put("gzi-gzi", "violin");
        instrumentSounds.put("boum-boum", "drum");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(Auditor::listenForUdp);
            executor.submit(Auditor::listenForTcp);
        }
    }

    private static void listenForTcp() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            while (true) {
                try (Socket socket = serverSocket.accept();
                     var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8))){

                    musicians.removeIf(musician -> musician.getLastActivity() < System.currentTimeMillis() - 5000);

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
        while (true) {
            try (MulticastSocket socket = new MulticastSocket(UDP_PORT)) {
                InetSocketAddress group_address = new InetSocketAddress(IPADDRESS, UDP_PORT);
                NetworkInterface netif = NetworkInterface.getByName("eth0");
                socket.joinGroup(group_address, netif);

                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength(), UTF_8);

                Gson gson = new Gson();
                Musician musician = gson.fromJson(message, Musician.class);

                musician.setInstrument(instrumentSounds.get(musician.getSound()));

                musicians.add(musician);

                System.out.println(musician.getInstrument());

                System.out.println("Received message: " + message + " from " + packet.getAddress() + ", port " + packet.getPort());

                socket.leaveGroup(group_address, netif);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
