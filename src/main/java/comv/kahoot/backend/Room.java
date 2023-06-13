package comv.kahoot.backend;

import comv.kahoot.User;
import comv.kahoot.quiz.Question;
import comv.kahoot.quiz.Quiz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class Room {
    // Room attributes
    private String id;
    private static final HashMap<String, Room> runningRooms = new HashMap<>();

    // User attributes
    private final Map<User, Socket> userSockets = new HashMap<>();
    private final Map<User, String> receivedFromListeners = new HashMap<>();
    private final Set<Thread> userListeners = new HashSet<>();
    private final Set<DataOutputStream> userWriters = new HashSet<>();

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
        userListeners.add(new Thread(new ListeningThread(hostReceiver, host)));
        userWriters.add(hostSender);

        runningRooms.put(id, this);

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

    protected void host() throws IOException {
        String packet = "h" + id;
        hostSender.writeUTF(packet);
        Server.log("Sent package: " + packet);
    }

    protected void join(Socket userSocket, DataOutputStream writer, DataInputStream receiver, String username) throws IOException, IllegalStateException {
        if(!state.equals(RoomState.NOT_READY))
            throw new IllegalStateException();

        User user = new User(username, 0, 0);

        Thread listener = new Thread(new ListeningThread(receiver, user));
        Server.threads.add(listener);
        userSockets.put(user, userSocket);
        userListeners.add(listener);
        userWriters.add(writer);
    }

    protected void start() throws InterruptedException, IOException {
        for(User user : userSockets.keySet())
            receivedFromListeners.put(user, "");

        boolean roomFinished = false;

        while(!roomFinished) {
            switch (state) {
                case NOT_READY:
                    // Scans until host says ready
                    while(!hostReceiver.readUTF().equals("r"));
                    state = RoomState.RUNNING;

                case RUNNING:
                    Quiz quiz = new Quiz("Cool quiz", new ArrayList<Question>(){{
                        this.add(new Question("What is the capital of France?", new String[]{"Paris", "London", "Berlin", "Madrid"}, 30, new int[4]));
                    }});

                    for(Question question : quiz.getQuestions()) {
                        String msg1 = "q" + question.getQuestion() + ";" + String.join(";", question.getAnswers());
                        sendToAllUsers(msg1);

                        LocalDateTime now = LocalDateTime.now();
                        for(Thread listener : userListeners)
                            listener.start();

                        int answered = 0;

                        while(answered < userSockets.size()){

                            for(Map.Entry<User, String> received : receivedFromListeners.entrySet()){
                                String msg2 = received.getValue();   // The full string received from the user

                                String userQuestion;
                                String answersEncoded;

                                try {
                                    userQuestion = msg2.substring(1, msg2.indexOf(';'));   // The question the user sent (to check if he answered to the current and not the last question)
                                    answersEncoded = msg2.substring(msg2.indexOf(';') + 1);    // The answers encoded in binary, ex: "0010"
                                } catch (Exception e){
                                    continue;
                                }
                                // Regex: a<some question>;<answersEncoded>
                                if(msg2.matches("^a[^;]*;\\d{4}$") && userQuestion.equals(question.getQuestion())) {
                                    calculateAndSetScore(now, received.getKey(), question, answersEncoded);
                                    answered++;
                                }
                            }
                        }

                        Server.log("All users answered");

                        for(Thread listener : userListeners){
                            listener.join();
                        }

                        StringBuilder correctAnswers = new StringBuilder();
                        for(int i = 0; i < question.getAnswers().length; i++)
                            correctAnswers.append('0');

                        for(int answerEncoded : question.getIndex())
                            correctAnswers.setCharAt(answerEncoded, '1');

                        sendToAllUsers("c" + correctAnswers.toString());
                        synchronized (this) {
                            wait(1000);
                        }

                        sendToAllUsers(sortedUsersAsProtocol());

                        while(!hostReceiver.readUTF().equals("r"));
                    }

                    sendToAllUsers("f");
                    roomFinished = true;
                    break;
            }
        }

        runningRooms.remove(id);
    }

    protected static Room getRoom(String id){
        return runningRooms.get(id);
    }

    private void sendToAllUsers(String msg) throws IOException {
        for(DataOutputStream writer : userWriters)
            writer.writeUTF(msg);
        Server.log("Sent package: " + msg);
    }

    protected static void checkUsername(String username) throws IllegalArgumentException {
        if(username.length() < 5 || username.length() > 20 || username.contains(";"))
            throw new IllegalArgumentException("h00000");
    }

    private void calculateAndSetScore(LocalDateTime startTime, User user, Question question, String answersEncoded) {
        Duration duration = Duration.between(startTime, LocalDateTime.now());

        ArrayList<Integer> answersList = new ArrayList<>();
        for(int i = 0; i < answersEncoded.length(); i++)
            if(answersEncoded.charAt(i) == '1')
                answersList.add(i);

        Integer[] answers = new Integer[answersList.size()];
        answersList.toArray(answers);

        // One correct answer
        if(question.getIndex().length == 1) {
            user.setScore(user.getScore() + (int)(1000 * (1 - ((duration.getSeconds() / question.getMaxSeconds()) / 2))));
        // Multiple answers
        }else{
            user.setScore(user.getScore() + (int)((500 * question.getIndex().length) * (1 - (((double)duration.getSeconds() / question.getMaxSeconds()) / 2))));
        }
    }

    private String sortedUsersAsProtocol(){
        List<User> sortedUsers = new ArrayList<>(userSockets.keySet());
        sortedUsers.sort((user1, user2) -> Integer.compare(user2.getScore(), user1.getScore()));

        StringBuilder result = new StringBuilder("p");
        for(User user : userSockets.keySet())
            result.append(user.getUsername() + ";" + user.getScore() + ";");
        result.deleteCharAt(result.lastIndexOf(";"));

        return result.toString();
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
                receivedFromListeners.put(user, listener.readUTF());
            } catch (IOException connectionLost) {
                Thread.currentThread().interrupt();
            }
        }
    }
}