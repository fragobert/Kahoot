package comv.kahoot.backend;

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
    protected final static Set<Thread> threads = new HashSet<>();
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
                Socket socket = serverSocket.accept();
                Thread connection = new Thread(new Connection(socket));
                log("New client connection: " + socket.getInetAddress());
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
                        terminal("Are you sure you want to stop the server?\nThis will close all running games! (y/n)");
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
                terminal("All threads stopped!");
            } catch (SecurityException e){
                System.err.println("Failed to stop all threads!");
            }

            terminal("Closing server...");
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
                log("Received package: " + answer + " (" + userSocket.getInetAddress() + ")");
                char action = answer.charAt(0);
                String args = answer.substring(1);

                while(true) {
                    switch (action) {
                        case 'h':
                            // Host a new room
                            try {
                                Room room = new Room(userSocket, receiver, sender, args);
                                log("Room created for: " + userSocket.getInetAddress());
                                room.host();
                                room.start();
                            } catch (IllegalArgumentException invUsername){
                                sender.writeUTF("00000");
                                break;
                            } catch (IllegalStateException tooManyRooms) {
                                sender.writeUTF("00001");
                                break;
                            }

                            break;

                        case 'j':
                            String id = args.substring(0, 4);
                            String username = args.substring(4);
                            try {
                                Room.checkUsername(username);
                            } catch (IllegalArgumentException invUsername){
                                sender.writeUTF("efailureInvalid username!");
                                break;
                            }

                            Room room = Room.getRoom(id);
                            if(room == null){
                                sender.writeUTF("efailureInvalid Room ID!");
                                break;
                            }
                            try{
                                room.join(userSocket, sender, receiver, username);
                            } catch (IllegalStateException roomRunning){
                                sender.writeUTF("efailureRoom already started!");
                                break;
                            }

                            room.join(userSocket, sender, receiver, username);
                            sender.writeUTF("esuccess");
                            return;

                        default:
                            // ignores request
                            break;
                    }
                }
            } catch (IOException | InterruptedException e) {
                criticalException("Disconnected! (" + userSocket.getInetAddress() + ", " + e.getMessage() + ")");
            }
        }

        private void criticalException(String message){
            log("Critical Exception: " + message);
            Thread.currentThread().interrupt();
        }
    }

    protected static void log(String msg){
        System.out.println("[LOG] " + msg);
    }

    protected static void terminal(String msg){
        System.out.println("[Terminal] " + msg);
    }

    public static void main(String[] args) {
        try {
            Server server = new Server("Kahoot Server", 6000);
            server.startup();
        } catch (IOException e) {
            Server.terminal("Server crashed!");
        }

    }
}
