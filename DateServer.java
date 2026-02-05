import java.io.*;
import java.net.*;

public class DateServer {
    public static void main(String[] args) throws Exception {
        int port = 6013;
        ServerSocket serverSocket = new ServerSocket(port);
        String serverIP = InetAddress.getLocalHost().getHostAddress();

        System.out.println("==================================");
        System.out.println("Javeed's Socket Server Started");
        System.out.println("Connected to WiFi IP: " + serverIP);
        System.out.println("Port: " + port);
        System.out.println("==================================");

        while (true) {
            Socket client = serverSocket.accept();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            String type = in.readLine();

            if ("DISCOVERY".equals(type)) {
                out.println("SERVER_NAME:Javeed's Socket Server");
                out.println("SERVER_WIFI_IP:" + serverIP);
                client.close();
                continue;
            }

            if ("CLIENT".equals(type)) {
                System.out.println("Real client connected from: " + client.getInetAddress());
                System.out.println("Chat started. Type messages (END to stop)");

                BufferedReader keyboard = new BufferedReader(
                        new InputStreamReader(System.in));

                // THREAD TO RECEIVE
                Thread receiveThread = new Thread(() -> {
                    try {
                        String msg;
                        while ((msg = in.readLine()) != null) {
                            if (msg.equalsIgnoreCase("END")) {
                                System.out.println("Client ended the chat.");
                                client.close();
                                break;
                            }
                            System.out.println("Client: " + msg);
                        }

                        System.out.println("Client disconnected.");
                        client.close();
                    } catch (Exception ignored) {}
                });
                receiveThread.start();

                // SEND LOOP
                String serverMsg;
                while (true) {
                    if (client.isClosed()) break;

                    serverMsg = keyboard.readLine();
                    if (serverMsg == null) break;

                    try {
                        out.println(serverMsg);
                    } catch (Exception e) {
                        System.out.println("Client disconnected. Cannot send messages.");
                        break;
                    }

                    if (serverMsg.equalsIgnoreCase("END")) {
                        client.close();
                        break;
                    }
                }
            }
        }
    }
}