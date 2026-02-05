import java.net.*;
import java.io.*;
import java.util.*;

public class DateClient {

    // Get actual WiFi IPv4 address instead of 127.0.0.1
    private static String getWiFiIP() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            if (netint.isUp() && !netint.isLoopback() && netint.getName().equals("en0")) {
                for (InetAddress addr : Collections.list(netint.getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        }
        return "127.0.0.1";
    }

    static List<String> discoveredServers = new ArrayList<>();

    public static void main(String[] args) {
        try {
            String myIP = getWiFiIP();
            String subnet = myIP.substring(0, myIP.lastIndexOf("."));
            System.out.println("==================================");
            System.out.println("Client running on WiFi IP: " + myIP);
            System.out.println("Scanning for socket servers on same WiFi network...");
            System.out.println("==================================\n");

            // Scan IPs in subnet
            for (int i = 1; i < 255; i++) {
                String host = subnet + "." + i;
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, 6013), 50);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

                    String serverNameLine = in.readLine();
                    if (serverNameLine != null && serverNameLine.startsWith("SERVER_NAME")) {
                        String serverName = serverNameLine.split(":")[1];
                        discoveredServers.add(host + " - " + serverName);
                    }
                    socket.close();
                } catch (Exception ignored) {}
            }

            if (discoveredServers.isEmpty()) {
                System.out.println("No servers found.");
                return;
            }

            System.out.println("Available Servers:");
            for (int i = 0; i < discoveredServers.size(); i++) {
                System.out.println((i + 1) + ". " + discoveredServers.get(i));
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("\nSelect server number to connect: ");
            int choice = Integer.parseInt(scanner.nextLine()) - 1;

            String selectedIP = discoveredServers.get(choice).split(" ")[0];

            Socket sock = new Socket(selectedIP, 6013);
            BufferedReader bin = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));

            String serverName = bin.readLine();
            String serverIPLine = bin.readLine();
            String date = bin.readLine();

            System.out.println("\n==================================");
            System.out.println("Connection Established Over WiFi");
            System.out.println("Connected to Server: " + serverName.split(":")[1]);
            System.out.println("Server WiFi IP: " + serverIPLine.split(":")[1]);
            System.out.println("Client WiFi IP: " + myIP);
            System.out.println("Server Date & Time: " + date.split(":")[1]);
            System.out.println("==================================");

            sock.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}