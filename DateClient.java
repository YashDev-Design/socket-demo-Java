import java.io.*;
import java.net.*;
import java.util.*;

public class DateClient {
    public static void main(String[] args) throws Exception {
        String clientIP = InetAddress.getLocalHost().getHostAddress();

        System.out.println("==================================");
        System.out.println("Client running on WiFi IP: " + clientIP);
        System.out.println("Scanning for socket servers on same WiFi network...");
        System.out.println("==================================");

        List<String> servers = new ArrayList<>();
        String subnet = clientIP.substring(0, clientIP.lastIndexOf('.') + 1);

        for (int i = 1; i < 255; i++) {
            String host = subnet + i;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, 6013), 100);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("DISCOVERY");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                String name = in.readLine();
                if (name != null && name.startsWith("SERVER_NAME")) {
                    servers.add(host);
                }
            } catch (Exception ignored) {}
        }

        if (servers.isEmpty()) {
            System.out.println("No servers found.");
            return;
        }

        System.out.println("Available Servers:");
        for (int i = 0; i < servers.size(); i++) {
            System.out.println((i + 1) + ". " + servers.get(i) + " - Javeed's Socket Server");
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("Select server number to connect: ");
        int choice = sc.nextInt();
        sc.nextLine();

        String serverIP = servers.get(choice - 1);
        Socket socket = new Socket(serverIP, 6013);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("CLIENT");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        BufferedReader keyboard = new BufferedReader(
                new InputStreamReader(System.in));

        System.out.println("\n==================================");
        System.out.println("Connection Established Over WiFi");
        System.out.println("Connected to Server: Javeed's Socket Server");
        System.out.println("Server WiFi IP: " + serverIP);
        System.out.println("Client WiFi IP: " + clientIP);
        System.out.println("Chat started. Type messages (END to stop)");
        System.out.println("==================================");

        // THREAD TO RECEIVE
        Thread receiveThread = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.equalsIgnoreCase("END")) {
                        System.out.println("Server ended the chat.");
                        socket.close();
                        break;
                    }
                    System.out.println("Server: " + msg);
                }
                // If loop exits normally
                System.out.println("Server disconnected. Reconnect to " + serverIP + " to chat again.");
                socket.close();
            } catch (Exception ignored) {}
        });
        receiveThread.start();

        // SEND LOOP
        String clientMsg;
        while (true) {
            if (socket.isClosed()) break;

            clientMsg = keyboard.readLine();
            if (clientMsg == null) break;

            try {
                out.println(clientMsg);
            } catch (Exception e) {
                System.out.println("Server disconnected. Cannot send messages.");
                break;
            }

            if (clientMsg.equalsIgnoreCase("END")) {
                socket.close();
                break;
            }
        }
    }
}



//i even want a feature like if server or client without closing the terminal by mistake type end and messaging ends then i want a message to ask me like you want to reconnect to server back and same message for server side you wish to recconnect with client back and when selected yes then on client side it should repeat the process scanning for servers availabe on same ip as before first time and on server side it should show waiting for client to get connecting back 
