# 📡 Advanced Java Socket Communication Demo (Client–Server with WiFi Detection)

This project demonstrates **TCP socket communication in Java** with **real network interface detection** and **LAN server discovery**.

It proves that the client and server communicate over an **actual WiFi network**, not loopback (`127.0.0.1`).  
The program was tested successfully by switching between:

- Home WiFi  
- Mobile Hotspot  

The IP address changed accordingly, and communication continued to work — confirming real network-based socket communication.

---

## 🎯 Objective

To demonstrate:

- TCP socket programming  
- Client–Server architecture  
- LAN-based communication  
- Real WiFi IP detection  
- Server discovery on the same subnet  

---

## 🧠 Key Features Implemented

| Feature | Description |
|--------|-------------|
| WiFi Interface Detection | Program fetches real IPv4 from network interface (en0) |
| Loopback Avoidance | Prevents `127.0.0.1` from being used |
| LAN Server Discovery | Client scans subnet for active servers |
| Server Identification | Server sends its name and WiFi IP |
| Interactive Selection | Client selects server from discovered list |
| Cross-Network Test | Works when switching WiFi networks |
| Connection-Type Handshake | Differentiates discovery scans from real client connections |

---

## 🗂 Project Structure

```
socket-demo/
│
├── DateServer.java   → Named server with WiFi IP display
├── DateClient.java   → LAN scanner + interactive client
└── README.md
```

---

## ⚙️ Requirements

- macOS / Linux / Windows  
- Java JDK installed

Check Java:

```bash
java -version
javac -version
```

---

## 🌐 Step 1 — Confirm Your WiFi IP Address

Before running the programs, verify your system is connected to WiFi:

```bash
ipconfig getifaddr en0
```

Example outputs (network changes depending on WiFi):

| Network | IP Address |
|---------|------------|
| Home WiFi | 192.168.0.10 |
| Mobile Hotspot | 172.20.10.5 |

This proves the device is connected to a real network.

---

## 🚀 Step 2 — Compile Programs

```bash
javac DateServer.java
javac DateClient.java
```

---

## 🖥️ Step 3 — Run Server

```bash
java DateServer
```

Example output:

```
==================================
Javeed's Socket Server Started
Connected to WiFi IP: 192.168.0.10
Port: 6013
==================================
```

This confirms the server is bound to the WiFi network.

---

## 💻 Step 4 — Run Client

```bash
java DateClient
```

Example:

```
==================================
Client running on WiFi IP: 192.168.0.10
Scanning for socket servers on same WiFi network...
==================================

Available Servers:
1. 192.168.0.10 - Javeed's Socket Server

Select server number to connect: 1
```

---

## 🔗 Step 5 — Connection Established

```
==================================
Connection Established Over WiFi
Connected to Server: Javeed's Socket Server
Server WiFi IP: 192.168.0.10
Client WiFi IP: 192.168.0.10
Server Date & Time: Wed Feb 05 14:22:01 CST 2026
==================================
```

---

## 🧪 Network Switching Test

The system was tested by switching networks:

| Scenario | Result |
|---------|--------|
| Home WiFi → Mobile Hotspot | IP changed automatically |
| Server restarted | Bound to new IP |
| Client scan | Discovered server on new subnet |
| Connection | Successful |

This proves communication is happening over **real LAN networking**.

---

## 🔍 Verification Commands

Check server listening on WiFi IP:

```bash
lsof -i :6013
```

Check network interface:

```bash
ifconfig en0 | grep inet
```

---

## 📶 Communication Flow

```
Client scans subnet → Finds server → User selects server
→ TCP connection established → Server sends name, IP, date
```

---

## 🤝 Connection Handshake Protocol (Latest Update)

To improve how the server logs connections, an **application-layer handshake** was added between the client and server.

### 🔍 Discovery Phase
When the client scans the network to find servers, it sends:

```
DISCOVERY
```

The server recognizes this as a **scan request** and:
- Sends back server name and IP
- **Does NOT log** it as a real client connection

### 👤 Real Client Connection
After the user selects a server, the client sends:

```
CLIENT
```

The server recognizes this as an actual user and:
- Logs the connection:
  ```
  Real client connected from: <client-ip>
  ```
- Sends server name, WiFi IP, and date

### 🎯 Why This Was Needed

Earlier, the server printed “Client connected” even during scanning because every scan is still a TCP connection.  
By adding this handshake message, the server can now distinguish:

| Type | Purpose |
|------|--------|
| DISCOVERY | Temporary scan connection |
| CLIENT | Actual user communication |

This design mimics how real-world protocols identify request types over TCP.

---

## 🆕 Multi‑Client Chat Upgrade (Latest Version)

The server was upgraded from a single-client model to a **multi-client concurrent architecture**.

### 🚀 What Was Added

| Feature | Description |
|----------|------------|
| Multi-Client Support | Multiple clients can connect simultaneously |
| Thread-Based Handling | Each client runs in its own `ClientHandler` thread |
| Unique Username Enforcement | Server asks for client name and prevents duplicates |
| Concurrent Client Storage | Active clients stored in `ConcurrentHashMap` |
| Connection List Tracking | Server maintains live list of connected clients |
| Clean Disconnect Handling | Server detects client termination and removes from list |
| Privacy Mode | Messages are NOT broadcast to other clients |

---

## 🧵 Architecture Change

### Before
- Server handled only one client at a time.
- Messages were broadcast to all connected clients.

### After
- Each client connection is handled by a separate thread.
- Clients are stored in:

```
ConcurrentHashMap<String, ClientHandler> clients
```

This allows:
- Thread-safe access
- Fast lookups
- Duplicate name prevention
- Automatic removal on disconnect

---

## 👤 Username Validation Logic

When a client connects:

1. Server sends:
```
ENTER_NAME
```

2. Client submits a name.

3. Server checks:
```
if (clients.containsKey(name))
```

4. If duplicate:
```
NAME_EXISTS
```

5. If unique:
```
NAME_ACCEPTED
```

This guarantees no two clients share the same identity.

---

## 🔐 Privacy Improvement (Broadcast Removed)

Originally, the server broadcasted messages like:

```
for (ClientHandler ch : clients.values())
```

This caused:
- Messages from one client appearing in other client terminals
- Privacy concerns

### Updated Behavior
Broadcast logic was removed.

Now:
- Messages sent by a client are visible **only on the server terminal**
- Other clients do NOT receive those messages

This converts the system into a **client-to-server communication model**, not a group chat.

---

## 🔌 Disconnection Handling

If a client closes its terminal:
- Server detects stream closure
- Removes client from `clients` map
- Updates active client list

If server closes:
- Client receives connection error
- Client must reconnect to resume communication

This ensures proper resource cleanup and stable multi-user behavior.

---

## 📊 Updated Communication Model

```
Multiple Clients
        ↓
   Thread per Client
        ↓
  Server (ConcurrentHashMap)
        ↓
   Server Console Logging
```

The server now behaves similarly to a centralized messaging server with controlled privacy and structured client management.

---