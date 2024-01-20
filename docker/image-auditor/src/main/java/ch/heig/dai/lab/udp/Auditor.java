package ch.heig.dai.lab.udp;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.DatagramPacket;
import java.util.ArrayList;
import com.google.gson.Gson;

import static java.nio.charset.StandardCharsets.*;


public class Auditor {
    final static String IPADDRESS = "239.255.22.5";
    final static int PORT = 9904;

    static ArrayList<Object> musicians = new ArrayList<>();

    public static void main(String[] args) {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetSocketAddress group_address =  new InetSocketAddress(IPADDRESS, PORT);
            NetworkInterface netif = NetworkInterface.getByName("eth0");
            socket.joinGroup(group_address, netif);

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength(), UTF_8);

            Gson gson = new Gson();
            Musician musician = gson.fromJson(message, Musician.class);

            musicians.add(musician);

            System.out.println("Received message: " + message + " from " + packet.getAddress() + ", port " + packet.getPort());
            socket.leaveGroup(group_address, netif);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
