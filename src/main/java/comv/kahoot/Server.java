package comv.kahoot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
/*
public class Server {
	
	private static void calculateScore(User user, Question question, Answer answer) {
		if(CompareArrays.compareArray(question.getIndex(), answer.getIndex())) {
			Duration duration = Duration.between(question.getStartTime(), answer.getEndtime());
			if(question.getIndex().length == 1) {
				user.setScore(user.getScore() + (int)(1000 * (1 - ((duration.getSeconds() / question.getMaxSeconds()) / 2))));
			}else{
				user.setScore(user.getScore() + (int)((500 * question.getIndex().length) * (1 - (((double)duration.getSeconds() / question.getMaxSeconds()) / 2))));
				System.out.println(question.getMaxSeconds());
			}
		}		
	}
	
	/*

	public static void main(String[] args) {
		User user = new User(0, 0);
		String[] answers = {"a", "b", "c", "d"};
		int[] indexQ = {1, 2, 3};
		LocalDateTime startTime = LocalDateTime.now();
		System.out.println(startTime.getSecond());
		Question question = new Question("Was ist richtig?", answers, 30, indexQ, startTime);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int[] indexA = {1, 2, 3};
		LocalDateTime endTime = LocalDateTime.now();
		System.out.println(endTime.getSecond());
		Answer answer = new Answer(indexA, endTime);
		
		calculateScore(user, question, answer);
		
		System.out.println(user.getScore());
	}


=====
package comv.kahoot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

*/

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
/*
public class Server {

	private static void calculateScore(User user, Question question, Answer answer) {
		if(CompareArrays.compareArray(question.getIndex(), answer.getIndex())) {
			Duration duration = Duration.between(question.getStartTime(), answer.getEndtime());
			if(question.getIndex().length == 1) {
				user.setScore(user.getScore() + (int)(1000 * (1 - ((duration.getSeconds() / question.getMaxSeconds()) / 2))));
			}else{
				user.setScore(user.getScore() + (int)((500 * question.getIndex().length) * (1 - (((double)duration.getSeconds() / question.getMaxSeconds()) / 2))));
				System.out.println(question.getMaxSeconds());
			}
		}
	}

	/*

	public static void main(String[] args) {
		User user = new User(0, 0);
		String[] answers = {"a", "b", "c", "d"};
		int[] indexQ = {1, 2, 3};
		LocalDateTime startTime = LocalDateTime.now();
		System.out.println(startTime.getSecond());
		Question question = new Question("Was ist richtig?", answers, 30, indexQ, startTime);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int[] indexA = {1, 2, 3};
		LocalDateTime endTime = LocalDateTime.now();
		System.out.println(endTime.getSecond());
		Answer answer = new Answer(indexA, endTime);

		calculateScore(user, question, answer);

		System.out.println(user.getScore());
	}


=====
package comv.kahoot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

*/

/**
 * Server hosts multiple rooms
 */
public class Server {
    private final ServerSocket serverSocket;
    private final static Set<Thread> threads = new HashSet<>();
    private final String name;

    public Server(String name, int port) throws IOException {
        this.name = name;
        serverSocket = new ServerSocket(port);
    }

    public String getName() {
        return name;
    }

    public void startup(){
        int id = 0;
        try {
            Thread terminal = new Thread(new Terminal());
            terminal.start();

            // Always accept new connections and cre
            while(true){
                Thread connection = new Thread(new Connection(serverSocket.accept()));
                System.out.println("New client connection: " + serverSocket.getInetAddress());
                connection.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Terminal implements Runnable {
        Scanner sc = new Scanner(System.in);

        @Override
        public void run() {
            boolean shutdown = false;

            System.out.println("- Kahoot Server Admin Terminal -");
            while(!shutdown){
                String cmd = sc.nextLine();

                switch(cmd){
                    case "stop":
                        System.out.println("Are you sure you want to stop the server?\nThis will close all running games! (y/n)");
                        String answer = sc.nextLine();
                        if(answer.equals("y")) {
                            shutdown = true;
                        }
                        break;

                    default:
                        System.err.println("Unknown command: " + cmd);
                }
            }
            try {
                threads.forEach(Thread::interrupt);
                System.out.println("All threads stopped!");
            } catch (SecurityException e){
                System.err.println("Failed to stop all threads!");
            }

            System.out.println("Closing server...");
            System.exit(0);
        }
    }

    /**
     * A thread to handle a single connection to a client
     */
    private class Connection implements Runnable {

        private final Socket userSocket;
        private DataInputStream receiver;
        private DataOutputStream sender;

        private Connection(Socket userSocket) {
            this.userSocket = userSocket;
            try {
                this.receiver = new DataInputStream(userSocket.getInputStream());
                this.sender = new DataOutputStream(userSocket.getOutputStream());
            } catch (IOException e) {
                criticalException("Failed to get input/output streams from socket! (" + userSocket.getInetAddress() + ", " + e.getMessage() + ")");
            }
        }

        @Override
        public void run() {
            // When Client connects successfully, it should send server a message with its next action (host or join)
            try {
                String answer = receiver.readUTF();
                char action = answer.charAt(0);
                String args = answer.substring(1);

                while(true) {
                    switch (action) {
                        case 'h':
                            // Host a new room
                            try {
                                Room room = new Room(userSocket, receiver, sender, args);
                                room.host();
                            } catch (IllegalArgumentException invUsername){
                                sender.writeUTF("00000");
                                break;
                            } catch (IllegalStateException tooManyRooms) {
                                sender.writeUTF("00001");
                                break;
                            }

                            break;

                        case 'j':
                            break;

                        default:
                            // ignore request
                            break;
                    }
                }
            } catch (IOException e) {
                criticalException("Disconnected! (" + userSocket.getInetAddress() + ", " + e.getMessage() + ")");
            }
        }

        private void criticalException(String message){
            System.out.println(message);
            Thread.currentThread().interrupt();
        }
    }


    // Ignore for now
    private class Room {

        private static int totalRoomsCreated = 0;
        private static final Set<Room> runningRooms = new HashSet<>();

        private final String id;

        // player name + socket
        private final Map<String, Socket> playerSockets = new HashMap<>();
        private final Socket hostSocket;
        private final DataInputStream receiver;
        private final DataOutputStream sender;

        protected Room(Socket hostSocket, DataInputStream receiver, DataOutputStream sender, String username) throws IllegalStateException, IllegalArgumentException {
            this.id = generateUniqueID();   // throws IllegalStateException if too many rooms
            checkUsername(username);        // throws IllegalArgumentException if too short, long or ';' contained

            this.hostSocket = hostSocket; this.receiver = receiver; this.sender = sender;

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
                    throw new IllegalStateException("Too many running rooms!");

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
                throw new IllegalArgumentException("Invalid username");
        }

        private void host(){

        }
    }
}
