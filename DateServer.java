import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class DateServer {

    private static final int PORT = 6013;

    // Thread-safe storage for connected clients
    private static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, ClientHandler> clientIds = new ConcurrentHashMap<>();
    private static int clientCounter = 0;


    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(PORT);
        String serverIP = InetAddress.getLocalHost().getHostAddress();

        System.out.println("==================================");
        System.out.println("Javeed's Multi-Client Socket Server Started");
        System.out.println("Connected to WiFi IP: " + serverIP);
        System.out.println("Port: " + PORT);
        System.out.println("==================================");

        // Thread to listen for server-side commands
        new Thread(() -> {
            try {
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
                String command;
                while ((command = serverInput.readLine()) != null) {
                    if (command.equalsIgnoreCase("show clients")) {
                        System.out.println("\n===== Connected Clients =====");
                        if (clientIds.isEmpty()) {
                            System.out.println("No clients currently connected.");
                        } else {
                            for (String id : clientIds.keySet()) {
                                ClientHandler ch = clientIds.get(id);
                                System.out.println(id + " -> " + ch.clientName);
                            }
                        }
                        System.out.println("LIVE CLIENT COUNT: " + clients.size());
                        System.out.println("=============================\n");
                    }
                    else if (command.startsWith("send ")) {
                        try {
                            String[] parts = command.split(" ", 3);
                            String targetId = parts[1];
                            String msg = parts[2];

                            ClientHandler target = clientIds.get(targetId);
                            if (target != null) {
                                target.out.println("[PRIVATE FROM SERVER]: " + msg);
                                System.out.println("Message sent to " + targetId);
                            } else {
                                System.out.println("Invalid Client ID.");
                            }
                        } catch (Exception e) {
                            System.out.println("Usage: send C1 Your message here");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading server command.");
            }
        }).start();

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }

    static class ClientHandler implements Runnable {

        private Socket socket;
        private String clientName;
        private BufferedReader in;
        private PrintWriter out;
        private String clientId;


        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String type = in.readLine();

                // Handle discovery phase
                if ("DISCOVERY".equals(type)) {
                    out.println("SERVER_NAME:Javeed's Multi-Client Server");
                    socket.close();
                    return;
                }

                if ("CLIENT".equals(type)) {

                    // Ask for client name
                    out.println("ENTER_NAME");
                    clientName = in.readLine();

                    // Ensure unique name
                    while (clients.containsKey(clientName)) {
                        out.println("NAME_EXISTS");
                        clientName = in.readLine();
                    }

                    clients.put(clientName, this);

                    synchronized (DateServer.class) {
                        clientCounter++;
                        clientId = "C" + clientCounter;
                    }

                    clientIds.put(clientId, this);


                    System.out.println("\n" + clientName + " (" + clientId + ") has JOINED the server.");
                    System.out.println("Active Clients:");
                    for (String id : clientIds.keySet()) {
                        ClientHandler ch = clientIds.get(id);
                        System.out.println(id + " -> " + ch.clientName);
                    }
                    System.out.println("LIVE CLIENT COUNT: " + clients.size());
                    System.out.println();

                    out.println("NAME_ACCEPTED");

                    String message;
                    while ((message = in.readLine()) != null) {

                        if (message.equalsIgnoreCase("END")) break;

                        System.out.println(clientName + ": " + message);

                        // Do NOT broadcast to other clients (private messaging to server only)
                        // Message is only printed on server console
                    }
                }

            } catch (Exception e) {
                System.out.println("Client disconnected: " + clientName);
            } finally {
                if (clientName != null) {
                    clients.remove(clientName);
                    clientIds.remove(clientId);
                    System.out.println("\n" + clientName + " (" + clientId + ") has LEFT the server.");
                    System.out.println("Remaining Active Clients:");
                    for (String id : clientIds.keySet()) {
                        ClientHandler ch = clientIds.get(id);
                        System.out.println(id + " -> " + ch.clientName);
                    }
                    System.out.println("LIVE CLIENT COUNT: " + clients.size());
                    System.out.println();
                }
                try { socket.close(); } catch (Exception ignored) {}
            }
        }
    }
}