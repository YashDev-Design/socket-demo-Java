import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class DateServer {

    private static final int PORT = 6013;

    // Stores clients by name and by id
    private static final ConcurrentHashMap<String, ClientHandler> clientsByName = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ClientHandler> clientsById = new ConcurrentHashMap<>();
    private static int clientCounter = 0;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        String serverIP = InetAddress.getLocalHost().getHostAddress();

        System.out.println("==================================");
        System.out.println("Javeed's Messenger Server Started");
        System.out.println("Connected to WiFi IP: " + serverIP);
        System.out.println("Port: " + PORT);
        System.out.println("==================================");
        System.out.println("Server Commands:");
        System.out.println("show clients");
        System.out.println("send <clientId/clientName> <message>");
        System.out.println("broadcast <message>");
        System.out.println("kick <clientId/clientName>");
        System.out.println("==================================");

        // Thread for server-side commands
        new Thread(() -> {
            try {
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
                String command;
                while ((command = serverInput.readLine()) != null) {
                    handleServerCommand(command);
                }
            } catch (Exception e) {
                System.out.println("Error reading server command.");
            }
        }, "ServerConsoleThread").start();

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            Thread t = new Thread(handler, "ClientThread-" + socket.getInetAddress().getHostAddress());
            handler.setThreadName(t.getName());
            t.start();
        }
    }

    private static void handleServerCommand(String command) {
        if (command.equalsIgnoreCase("show clients")) {
            printClientDirectory();
            return;
        }

        if (command.toLowerCase().startsWith("send ")) {
            try {
                String[] parts = command.split(" ", 3);
                String targetKey = parts[1];
                String msg = parts[2];
                ClientHandler target = findClient(targetKey);
                if (target != null) {
                    target.send("[PRIVATE FROM SERVER]: " + msg);
                    System.out.println("Message sent to " + target.clientId + " (" + target.clientName + ")");
                } else {
                    System.out.println("Invalid client id/name.");
                }
            } catch (Exception e) {
                System.out.println("Usage: send C1 Hello there");
            }
            return;
        }

        if (command.toLowerCase().startsWith("broadcast ")) {
            try {
                String msg = command.substring(10).trim();
                broadcastToAll("[SERVER BROADCAST]: " + msg);
                System.out.println("Broadcast sent to all connected clients.");
            } catch (Exception e) {
                System.out.println("Usage: broadcast Hello everyone");
            }
            return;
        }

        if (command.toLowerCase().startsWith("kick ")) {
            try {
                String targetKey = command.substring(5).trim();
                ClientHandler target = findClient(targetKey);
                if (target != null) {
                    target.send("[SERVER]: You have been disconnected by the server.");
                    target.closeConnection();
                    System.out.println("Client kicked: " + target.clientId + " (" + target.clientName + ")");
                } else {
                    System.out.println("Invalid client id/name.");
                }
            } catch (Exception e) {
                System.out.println("Usage: kick C1");
            }
            return;
        }

        System.out.println("Unknown command.");
    }

    private static ClientHandler findClient(String key) {
        ClientHandler byId = clientsById.get(key);
        if (byId != null) return byId;
        return clientsByName.get(key);
    }

    private static synchronized String nextClientId() {
        clientCounter++;
        return "C" + clientCounter;
    }

    private static void printClientDirectory() {
        System.out.println("\n===== Connected Clients Directory =====");
        if (clientsById.isEmpty()) {
            System.out.println("No clients currently connected.");
        } else {
            for (String id : clientsById.keySet()) {
                ClientHandler ch = clientsById.get(id);
                System.out.println(id + " -> Name: " + ch.clientName
                        + ", IP: " + ch.clientIp
                        + ", Thread: " + ch.threadName);
            }
        }
        System.out.println("LIVE CLIENT COUNT: " + clientsByName.size());
        System.out.println("=======================================\n");
    }

    private static String buildClientListMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("CLIENT_LIST");
        if (clientsById.isEmpty()) {
            sb.append(": No clients connected");
            return sb.toString();
        }

        for (String id : clientsById.keySet()) {
            ClientHandler ch = clientsById.get(id);
            sb.append("\n")
              .append(id)
              .append(" -> Name: ")
              .append(ch.clientName)
              .append(", IP: ")
              .append(ch.clientIp)
              .append(", Thread: ")
              .append(ch.threadName);
        }
        return sb.toString();
    }

    private static void sendUpdatedClientListToAll() {
        String listMessage = buildClientListMessage();
        for (ClientHandler ch : clientsById.values()) {
            ch.send(listMessage);
        }
    }

    private static void broadcastToAll(String message) {
        for (ClientHandler ch : clientsById.values()) {
            ch.send(message);
        }
    }

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private String clientName;
        private String clientId;
        private String clientIp;
        private String threadName;
        private BufferedReader in;
        private PrintWriter out;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        void send(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        void closeConnection() {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }

        public void run() {
            try {
                clientIp = socket.getInetAddress().getHostAddress();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String type = in.readLine();

                // Handle discovery phase
                if ("DISCOVERY".equals(type)) {
                    out.println("SERVER_NAME:Javeed's Messenger Server");
                    out.println("SERVER_WIFI_IP:" + socket.getLocalAddress().getHostAddress());
                    closeConnection();
                    return;
                }

                if (!"CLIENT".equals(type)) {
                    closeConnection();
                    return;
                }

                // Ask for client name
                out.println("ENTER_NAME");
                clientName = in.readLine();

                while (clientName == null || clientName.trim().isEmpty() || clientsByName.containsKey(clientName)) {
                    out.println("NAME_EXISTS");
                    clientName = in.readLine();
                }

                clientId = nextClientId();
                clientsByName.put(clientName, this);
                clientsById.put(clientId, this);

                out.println("NAME_ACCEPTED:" + clientId);
                out.println("[SERVER]: Welcome " + clientName + "! Your client ID is " + clientId);
                out.println("[SERVER]: Commands -> LIST | MSG:<clientId/clientName>:<message> | BROADCAST:<message> | END");

                System.out.println("\n" + clientName + " (" + clientId + ") has JOINED the server.");
                printClientDirectory();
                sendUpdatedClientListToAll();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("END") || message.equalsIgnoreCase("EXIT")) {
                        out.println("[SERVER]: Disconnecting...");
                        break;
                    }

                    if (message.equalsIgnoreCase("LIST")) {
                        out.println(buildClientListMessage());
                        continue;
                    }

                    if (message.startsWith("BROADCAST:")) {
                        String broadcastMsg = message.substring("BROADCAST:".length()).trim();
                        if (!broadcastMsg.isEmpty()) {
                            broadcastToAll("[BROADCAST][" + clientName + "]: " + broadcastMsg);
                            System.out.println("[BROADCAST] " + clientName + ": " + broadcastMsg);
                        }
                        continue;
                    }

                    if (message.startsWith("MSG:")) {
                        String[] parts = message.split(":", 3);
                        if (parts.length == 3) {
                            String targetKey = parts[1].trim();
                            String privateMsg = parts[2].trim();

                            ClientHandler target = findClient(targetKey);
                            if (target != null) {
                                target.send("[PRIVATE][" + clientName + " -> you]: " + privateMsg);
                                out.println("[PRIVATE][you -> " + target.clientName + "]: " + privateMsg);
                                System.out.println("[PRIVATE] " + clientName + " -> " + target.clientName + ": " + privateMsg);
                            } else {
                                out.println("[SERVER]: Target client not found.");
                            }
                        } else {
                            out.println("[SERVER]: Invalid format. Use MSG:<clientId/clientName>:<message>");
                        }
                        continue;
                    }

                    // Default: message to server only
                    System.out.println(clientName + ": " + message);
                    out.println("[SERVER]: Message received by server.");
                }

            } catch (Exception e) {
                System.out.println("Client disconnected: " + clientName);
            } finally {
                if (clientName != null) {
                    clientsByName.remove(clientName);
                    clientsById.remove(clientId);

                    System.out.println("\n" + clientName + " (" + clientId + ") has LEFT the server.");
                    printClientDirectory();
                    sendUpdatedClientListToAll();
                }
                closeConnection();
            }
        }
    }
}