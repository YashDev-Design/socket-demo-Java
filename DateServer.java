import java.net.*;
import java.io.*;
import java.util.*;

public class DateServer {

    private static String getWiFiIP() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            if (netint.isUp() && !netint.isLoopback() && netint.getName().equals("en0")) {
                for (InetAddress addr : Collections.list(netint.getInetAddresses())) {
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        }
        return "Unknown";
    }

    public static void main(String[] args) {
        try {
            String serverIP = getWiFiIP();
            ServerSocket sock = new ServerSocket(6013);

            System.out.println("==================================");
            System.out.println("Javeed's Socket Server Started");
            System.out.println("Connected to WiFi IP: " + serverIP);
            System.out.println("Port: 6013");
            System.out.println("==================================");

            while (true) {
                Socket client = sock.accept();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                PrintWriter pout = new PrintWriter(client.getOutputStream(), true);

                // First message tells server what kind of connection this is
                String type = in.readLine();

                if ("DISCOVERY".equals(type)) {
                    // Discovery scan — do not log as real client
                    pout.println("SERVER_NAME:Javeed's Socket Server");
                    pout.println("SERVER_WIFI_IP:" + serverIP);
                } else if ("CLIENT".equals(type)) {
                    // Real user connection
                    System.out.println("Real client connected from: " + client.getInetAddress());

                    pout.println("SERVER_NAME:Javeed's Socket Server");
                    pout.println("SERVER_WIFI_IP:" + serverIP);
                    pout.println("DATE:" + new java.util.Date());
                }

                client.close();
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
}