import java.io.*;
import java.net.*;
import java.util.*;

public class DateClient {

    private static String getWiFiIP() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            if (netint.isUp() && !netint.isLoopback()) {
                for (InetAddress addr : Collections.list(netint.getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        }
        return "127.0.0.1";
    }

    public static void main(String[] args) {
        try {
            String myIP = getWiFiIP();
            String subnet = myIP.substring(0, myIP.lastIndexOf("."));

            System.out.println("==================================");
            System.out.println("Client running on WiFi IP: " + myIP);
            System.out.println("Scanning for socket servers on same WiFi network...");
            System.out.println("==================================\n");

            List<String> discoveredServers = new ArrayList<>();

            for (int i = 1; i < 255; i++) {
                String host = subnet + "." + i;
                try {
                    Socket scanSocket = new Socket();
                    scanSocket.connect(new InetSocketAddress(host, 6013), 60);

                    PrintWriter scanOut = new PrintWriter(scanSocket.getOutputStream(), true);
                    scanOut.println("DISCOVERY");

                    BufferedReader scanIn = new BufferedReader(
                            new InputStreamReader(scanSocket.getInputStream()));

                    String serverNameLine = scanIn.readLine();
                    if (serverNameLine != null && serverNameLine.startsWith("SERVER_NAME")) {
                        discoveredServers.add(host);
                    }

                    scanSocket.close();
                } catch (Exception ignored) {
                }
            }

            if (discoveredServers.isEmpty()) {
                System.out.println("No servers found.");
                return;
            }

            System.out.println("Available Servers:");
            for (int i = 0; i < discoveredServers.size(); i++) {
                System.out.println((i + 1) + ". " + discoveredServers.get(i) + " - Javeed's Messenger Server");
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("\nSelect server number to connect: ");
            int choice = Integer.parseInt(scanner.nextLine()) - 1;

            if (choice < 0 || choice >= discoveredServers.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            String serverIP = discoveredServers.get(choice);

            Socket socket = new Socket(serverIP, 6013);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("CLIENT");

            String response = in.readLine();

            String myName = "";
            String myClientId = "";

            if ("ENTER_NAME".equals(response)) {
                while (true) {
                    System.out.print("Enter your name: ");
                    myName = scanner.nextLine().trim();
                    out.println(myName);

                    String serverReply = in.readLine();

                    if ("NAME_EXISTS".equals(serverReply)) {
                        System.out.println("Name already taken. Enter different name.");
                    } else if (serverReply != null && serverReply.startsWith("NAME_ACCEPTED:")) {
                        myClientId = serverReply.split(":", 2)[1].trim();
                        System.out.println("Name accepted. You joined the chat.");
                        break;
                    } else {
                        System.out.println("Unexpected response from server.");
                        socket.close();
                        return;
                    }
                }
            } else {
                System.out.println("Unexpected server response.");
                socket.close();
                return;
            }

            System.out.println("\n==================================");
            System.out.println("Connection Established Over WiFi");
            System.out.println("Connected to Server: Javeed's Messenger Server");
            System.out.println("Server WiFi IP: " + serverIP);
            System.out.println("Client WiFi IP: " + myIP);
            System.out.println("Your Name: " + myName);
            System.out.println("Your Client ID: " + myClientId);
            System.out.println("==================================");
            System.out.println("Available Commands:");
            System.out.println("LIST");
            System.out.println("MSG:<clientId/clientName>:<message>");
            System.out.println("BROADCAST:<message>");
            System.out.println("END");
            System.out.println("==================================");

            Thread receiveThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println("\n" + msg);
                    }
                    System.out.println("\nDisconnected from server.");
                    socket.close();
                } catch (Exception e) {
                    System.out.println("\nServer disconnected.");
                }
            });

            receiveThread.start();

            while (true) {
                String message = scanner.nextLine();
                out.println(message);

                if ("END".equalsIgnoreCase(message) || "EXIT".equalsIgnoreCase(message)) {
                    socket.close();
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}