
# 📡 Java Socket-Based Messenger System

This project is a **multi-client messenger system** built using **Java Socket Programming**.  
It demonstrates real-world networking concepts including **multi-threading, client-server communication, message routing, and connection management**.

---

## 🚀 Features

### 🔵 Server Features
- Runs on a specific **IP and PORT (6013)**
- Handles **multiple clients simultaneously** using threads
- Maintains a **client directory**:
  - Client ID (C1, C2, ...)
  - Client Name
  - Client IP Address
  - Thread Name
- Ensures **unique client names** (no duplicates allowed)
- Automatically sends **updated client list** to all clients when:
  - A client joins
  - A client leaves
- Supports:
  - 📋 Show connected clients (`show clients`)
  - 📩 Send message to specific client (`send C1 hello`)
  - 📢 Broadcast message (`broadcast hello`)
  - ❌ Force disconnect client (`kick C1`)
- Routes messages:
  - Client → Server
  - Client → Specific Client
  - Client → All Clients (broadcast)

---

### 🟢 Client Features
- Connects to server using **IP and PORT**
- Automatically **discovers server on same WiFi network**
- Registers with a **unique username**
- Receives a **client ID** from server
- Supports commands:
  - 📋 `LIST` → Get all connected clients
  - 📩 `MSG:<clientId/name>:<message>` → Send private message
  - 📢 `BROADCAST:<message>` → Send message to all clients
  - ❌ `END` → Exit the program
- Uses **threads** to:
  - Send messages
  - Receive messages simultaneously

---

## 🧠 Architecture

### Multi-Client Design
Each client connection is handled by a separate thread:

Client 1 ─┐
Client 2 ─┼──> Server (Thread per client)
Client 3 ─┘

---

### Data Structure Used

```java
ConcurrentHashMap<String, ClientHandler>

Used for:
	•	Thread-safe client storage
	•	Fast lookup by name or ID
	•	Managing active connections

⸻

🔄 Communication Protocol

Command	Description
LIST	Get all connected clients
MSG:<target>:<message>	Send private message
BROADCAST:<message>	Send to all clients
END	Disconnect


⸻

🛠️ How to Run

1️⃣ Compile

javac DateServer.java
javac DateClient.java


⸻

2️⃣ Run Server

java DateServer


⸻

3️⃣ Run Client (multiple terminals)

java DateClient


⸻

🧪 Example Usage

Get Client List

LIST

Private Message

MSG:C2:Hello Jack

Broadcast Message

BROADCAST:Hello everyone

Exit

END


⸻

🔌 Server Commands

Run directly in server terminal:

show clients
send C1 hello
broadcast hello all
kick C2


⸻

🎓 Concepts Demonstrated
	•	Socket Programming (TCP)
	•	Multi-threading
	•	Client-Server Architecture
	•	Network Interface Handling
	•	Concurrent Data Structures
	•	Message Routing & Protocol Design
	•	Connection Lifecycle Management

⸻

📌 Conclusion

This project simulates a real-world messaging system using low-level Java sockets.
It demonstrates how scalable server systems handle multiple users, routing, and communication control.

⸻

👨‍💻 Author

Yash (Javeed)
Master’s in Computer Science
Operating Systems Project

---

