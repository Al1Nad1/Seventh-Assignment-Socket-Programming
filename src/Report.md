
# Seventh Assignment Socket Programming - Report

## 1. Three Ways to Send a Login Message

### Method 1: Plain String Format

**Code Example:**
```java
// Plain String Format
PrintWriter stringOut = new PrintWriter(socket.getOutputStream(), true);
stringOut.println("LOGIN|" + loginRequest.username + "|" + loginRequest.password);
```

- **Pros of using plain string format (e.g., `"LOGIN|user|pass"`):**
    - **Simplicity:** The message format is easy to implement. Just a plain string with simple delimiters.
    - **Efficiency:** It’s lightweight, meaning it has minimal overhead since you’re just sending a basic string.
    - **Human-readable:** It’s easy for humans to read and understand the format.

- **Cons of using plain string format:**
    - **Error-prone:** If the data (such as username or password) contains special characters (e.g., `|`), it can break the message format. This may cause issues with parsing.
    - **No data validation:** There's no mechanism to validate the types or structure of the data (i.e., ensuring that `username` and `password` are in the correct format).
    - **Limited extensibility:** As your system grows, it may become harder to add new fields without changing the structure of the string.

- **Parsing:**
  You can parse this string using `split()`:
  ```java
  String[] parts = receivedMessage.split("\|");
  String command = parts[0];  // LOGIN
  String username = parts[1];
  String password = parts[2];
  ```

  If the delimiter `|` appears in the data, it could lead to incorrect splitting, and your parsing logic would fail. A better approach would involve escaping characters or using a more structured format.

- **Suitability for complex or nested data:**
  This method is not suitable for complex or nested data. As the structure of the data grows or becomes more complicated (e.g., lists, objects), it will quickly become unmanageable. You’d need to resort to other methods like JSON or serialization.

---

### Method 2: Serialized Java Object

**Code Example:**
```java
// Serialized Java Object Format
ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
objectOut.writeObject(loginRequest);
```

- **Pros of using serialized Java objects:**
    - **Structured data:** The object is already structured and typed, so there’s no need to manually parse or separate fields.
    - **No need for delimiters:** Since the object is being serialized, you don’t need to worry about delimiters like in the plain string format.
    - **Ease of extension:** You can easily add new fields or modify the object without worrying about breaking the parsing logic.

- **Cons of using serialized Java objects:**
    - **Java-specific:** The object is serialized in Java-specific format, meaning non-Java clients won’t be able to interpret it. This limits the flexibility for cross-platform communication.
    - **Overhead:** Serialization and deserialization require extra computational resources, which can be a problem for large-scale systems.

- **Suitability for non-Java clients (e.g., Python):**
  Non-Java clients would not be able to deserialize a Java object. To enable cross-platform communication, you would need to use a language-agnostic format like JSON or XML.

---

### Method 3: JSON

**Code Example:**
```java
// JSON Format
Gson gson = new Gson();
String json = gson.toJson(loginRequest);
PrintWriter jsonOut = new PrintWriter(socket.getOutputStream(), true);
jsonOut.println(json);
```

- **Pros of using JSON format:**
    - **Cross-platform compatibility:** JSON is a language-agnostic format, meaning it can be read by almost any programming language (Java, Python, JavaScript, etc.).
    - **Human-readable:** JSON is easy to read and debug, making it ideal for logging and troubleshooting.
    - **Extensible:** JSON can easily handle nested data structures (e.g., arrays or objects), which makes it very flexible as your application grows.

- **Cons of using JSON:**
    - **Parsing overhead:** Parsing JSON requires libraries and some computational effort, though this is typically minimal compared to serialization.
    - **Not as efficient for simple data:** For simple strings or numbers, JSON might be overkill because it adds more complexity than a simple string message.

- **Suitability for cross-language communication:**
  JSON is a great choice for communication between systems written in different languages. Since it's widely supported across programming languages, both Java and non-Java clients can send and receive messages in JSON format.

---

## 2. Practical Questions

### Chat

- **Task Description:**
  The task involves building a basic chat system, where multiple users can join a chat room, send messages to each other, and receive real-time updates.

- **Key Steps:**
    1. **Message Receiving on the Server:**
        - The server listens for incoming messages from clients.
        - It processes the message and then broadcasts it to all other connected clients.

    2. **Sending Messages to Clients:**
        - Upon receiving a message from a client, the server sends that message to every other connected client.

    3. **Broadcasting Messages:**
        - The server sends the message to each connected client in the list of active connections.

    4. **Handling Client Disconnections:**
        - If a client disconnects, the server should remove the client from the list of active connections.
        - It should also close the socket and release any associated resources.

    5. **Client-side Message Receiver:**
        - The client should use a separate thread to listen for incoming messages from the server. This ensures that the user can continue interacting with the application while receiving messages.

    6. **Sending Messages from Client to Server:**
        - The client sends messages to the server by writing to the output stream connected to the server.

    7. **Exit Mechanism:**
        - The client can type `/exit` to terminate the connection with the server and close the application.

---

### File Upload / Download

- **Task Description:**
  In this task, the client can upload files to the server and download files from the server.

- **Key Steps:**
    1. **Uploading Files:**
        - The client sends the file metadata (e.g., filename, size) to the server first.
        - It then sends the file data as a stream of bytes to the server.

    2. **Server-side File Handling:**
        - The server receives the file data and saves it on the server’s filesystem.

    3. **Downloading Files:**
        - The client requests a list of files available on the server.
        - After choosing a file, the server sends the file data to the client.
        - The client saves the file to its local filesystem.

---

### Bonus Tasks

#### Chat History

To implement chat history:
- The server should maintain the last `n` messages in a queue.
- Whenever a new client connects, the server sends these `n` messages to the new client to give them the chat history.
- This ensures that new clients are not left out of the conversation when they first connect.

---

#### Chat GUI

For the bonus task of implementing a GUI:
- Use JavaFX or Swing to create a graphical interface.
- The GUI should include a text area for displaying messages, a text field for entering new messages, and buttons for sending messages or exiting the chat.
- The GUI should also display a list of active users in the chat room.

---

## Conclusion

In this report, we’ve explored several methods for sending messages between the client and server, including using plain strings, serialized Java objects, and JSON. We also described the core functionality for a basic chat application, including message handling, broadcasting, file upload/download features, and more. Finally, we covered the implementation of additional tasks, such as maintaining a chat history and creating a GUI for the chat system.

