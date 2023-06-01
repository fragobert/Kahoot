package comv.kahoot;

import javafx.scene.chart.PieChart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Pattern;

public class Room {
    private static final HashMap<String, Room> runningRooms = new HashMap<>();

    private String id;

    // player name + socket
    private final Map<String, Socket> userSockets = new HashMap<>();

    // Updates the inputs received from socket for the username key
    private final Map<String, String> received = new HashMap<>();

    private final Set<DataOutputStream> writers = new HashSet<>();

    private final Socket hostSocket;
    private final DataInputStream hostReceiver;
    private final DataOutputStream hostSender;
    protected String state = "j";

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

        userSockets.put(username, hostSocket);

        synchronized (runningRooms) {
            runningRooms.put(id, this);
        }
    }

    protected static Room getRoom(String id){
        return runningRooms.get(id);
    }

    private String generateUniqueID() throws IllegalStateException {
        String id = null;
        boolean unique = false;

        while(!unique){
            if(runningRooms.size() > 7500)
                throw new IllegalStateException("h00001");

            id = String.format("%04d", new Random().nextInt(9999) + 1);
            unique = true;

            for(Room room : runningRooms.values())
                if (room.id.equals(id)) {
                    unique = false;
                    break;
                }
        }

        return id;
    }

    protected static void checkUsername(String username) throws IllegalArgumentException {
        if(username.length() < 5 || username.length() > 20 || username.contains(";"))
            throw new IllegalArgumentException("h00000");
    }

    protected void host() throws IOException {
        String packet = "h" + id;
        hostSender.writeUTF(packet);
        Server.log("Sent package: " + packet);
    }

    protected void join(Socket userSocket, DataOutputStream sender, DataInputStream receiver, String username) throws IOException, InterruptedException {
        userSockets.put(username, userSocket);

        Thread listener = new Thread(new ListeningThread(receiver, username));
        Server.threads.add(listener);
        listener.start();

        writers.add(sender);
        listener.start();
        writer.start();

        recei.join();
    }

    protected void start(){

    }

    private class ListeningThread implements Runnable {
        private final String username;


        final DataInputStream listener;
        public ListeningThread(DataInputStream listener, String username){
            this.listener = listener;
            this.username = username;
        }

        public void run() {
            while(true){
                received.put(username, listener.readUTF())

            }
        }
    }
}