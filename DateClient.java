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

                String response = in.readLine();
                if (response != null && response.startsWith("SERVER_NAME")) {
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
            System.out.println((i + 1) + ". " + servers.get(i));
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Select server number to connect: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        String serverIP = servers.get(choice - 1);
        Socket socket = new Socket(serverIP, 6013);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        out.println("CLIENT");

        String response = in.readLine();

        if ("ENTER_NAME".equals(response)) {
            while (true) {
                System.out.print("Enter your name: ");
                String name = scanner.nextLine();
                out.println(name);

                String serverReply = in.readLine();

                if ("NAME_EXISTS".equals(serverReply)) {
                    System.out.println("Name already taken. Try another.");
                }
                else if ("NAME_ACCEPTED".equals(serverReply)) {
                    System.out.println("Name accepted. You joined the chat.");
                    break;
                }
            }
        }

        System.out.println("==================================");
        System.out.println("Connected to Server: " + serverIP);
        System.out.println("Chat started. Type messages (END to exit)");
        System.out.println("==================================");

        // Thread to receive messages
        Thread receiveThread = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (Exception e) {
                System.out.println("Disconnected from server.");
            }
        });
        receiveThread.start();

        // Sending loop
        while (true) {
            String message = scanner.nextLine();
            out.println(message);

            if ("END".equalsIgnoreCase(message)) {
                socket.close();
                break;
            }
        }
    }
}
