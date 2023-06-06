package comv.kahoot.backend;

import comv.kahoot.User;
import comv.kahoot.quiz.Quiz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Pattern;

public class Room {
    // Room attributes
    private String id;
    private static final HashMap<String, Room> runningRooms = new HashMap<>();

    // User attributes
    private final Map<User, Socket> userSockets = new HashMap<>();
    private final Map<User, String> received = new HashMap<>();
    private final Set<Thread> listeners = new HashSet<>();
    private final Set<DataOutputStream> writers = new HashSet<>();

    private final Socket hostSocket;
    private final DataInputStream hostReceiver;
    private final DataOutputStream hostSender;

    protected RoomState state = RoomState.NOT_READY;

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

        User host = new User(username, 0,0);
        userSockets.put(host, hostSocket);
        listeners.add(new Thread(new ListeningThread(hostReceiver, host)));
        writers.add(hostSender);

        runningRooms.put(id, this);

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

    protected void join(Socket userSocket, DataOutputStream writer, DataInputStream receiver, String username) throws IOException, InterruptedException {
        User user = new User(username, 0, 0);
        userSockets.put(user, userSocket);

        Thread listener = new Thread(new ListeningThread(receiver, user));
        Server.threads.add(listener);
        listeners.add(listener);

        writers.add(writer);
    }

    protected void start() throws InterruptedException {
        boolean roomFinished = false;

        while(!roomFinished) {
            switch (state) {
                case NOT_READY:
                    for(Thread listener : listeners){
                        listener.start();
                    }
                    for(Thread listener : listeners){
                        listener.join();
                    }

                    for(String msg : received.values()){
                        if(!msg.equals("r")){
                            // Shit's not implemented
                            return;
                        }
                    }

                    state = RoomState.RUNNING;
                    break;

                case RUNNING:
                    Quiz quiz = Quiz.loadQuiz();
            }
        }

        runningRooms.remove(id);
    }

    private class ListeningThread implements Runnable {
        private final User user;
        final DataInputStream listener;

        public ListeningThread(DataInputStream listener, User user){
            this.listener = listener;
            this.user = user;
        }

        public void run() {
            try {
                received.put(user, listener.readUTF());
            } catch (IOException connectionLost) {
                Thread.currentThread().interrupt();
            }
        }
    }
}