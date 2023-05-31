package comv.kahoot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Pattern;

public class Room {
    private static final Set<Room> runningRooms = new HashSet<>();

    private String id;

    // player name + socket
    private final Map<String, Socket> playerSockets = new HashMap<>();
    private final Set<DataInputStream> receivers = new HashSet<>();
    private final Set<DataOutputStream> senders = new HashSet<>();
    private final Socket hostSocket;
    private final DataInputStream hostReceiver;
    private final DataOutputStream hostSender;

    protected Room(Socket hostSocket, DataInputStream hostReceiver, DataOutputStream hostSender, String username) throws IllegalStateException, IllegalArgumentException, IOException {
        try {
            this.id = generateUniqueID();   // throws IllegalStateException if too many rooms
            checkUsername(username);        // throws IllegalArgumentException if too short, long or ';' contained
        } catch (Exception e){
            String msg = e.getMessage();
            // Check if exception is only made of digit
            if(!Pattern.matches("^\\d+$", msg))
                msg = "00009";
            hostSender.writeUTF(msg);
            Thread.currentThread().interrupt();
        }

        this.hostSocket = hostSocket; this.hostReceiver = hostReceiver; this.hostSender = hostSender;

        playerSockets.put(username, hostSocket);

        synchronized (runningRooms) {
            runningRooms.add(this);
        }
    }

    private String generateUniqueID() throws IllegalStateException {
        String id = null;
        boolean unique = false;

        while(!unique){
            if(runningRooms.size() > 7500)
                throw new IllegalStateException("h00001");

            id = String.format("%04d", new Random().nextInt(9999) + 1);
            unique = true;

            for(Room room : runningRooms)
                if (room.id.equals(id)) {
                    unique = false;
                    break;
                }
        }

        return id;
    }

    private void checkUsername(String username) throws IllegalArgumentException {
        if(username.length() < 5 || username.length() > 20 || username.contains(";"))
            throw new IllegalArgumentException("h00000");
    }

    private void host() throws IOException {
        String packet = "h" + id;
        hostSender.writeUTF(packet);
        Server.log("Sent package: " + packet);

        while(playerSockets.size() <= 50){

        }
    }



    protected class ListeningThread implements Runnable {
        public void run() {
            while(true){
                try {
                    String msg = in.readUTF();

                    if(msg.startsWith(PREFIX)) {
                        if(msg.equals(PREFIX + "exit")) {
                            System.out.println("Chat partner closed connection!");
                            stop = true;
                            return;
                        }
                    } else {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    if(stop) return;

                    System.out.println("Error reading from other end: " + e.getMessage());
                    return;
                }
            }
        }
    }

    protected class WritingThread implements Runnable {

        public void run() {
            while(true){
                try {
                    String msg = writingScanner.nextLine();

                    if(msg.startsWith(PREFIX)) {
                        out.writeUTF(msg);

                        if(msg.equals(PREFIX + "exit")) {
                            System.out.println("Closing connection...");
                            stop = true;
                            return;
                        }
                        else {
                            System.err.println("Unknown command: " + msg);
                        }
                    } else if(!msg.isBlank()) {
                        out.writeUTF("(" + type + ") " + name + ": " + msg);
                        out.flush();
                    }
                } catch (IOException e) {
                    System.out.println("Error writing to other end: " + e.getMessage());
                    return;
                } catch (IllegalStateException e) {
                    return;
                }
            }
        }
    }
}