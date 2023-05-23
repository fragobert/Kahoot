import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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
                Socket hostSocket = serverSocket.accept();
                Thread connection = new Thread(new Connection(hostSocket));
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
     * A thread to handle a single connection
     */
    private class Connection implements Runnable {

        private final Socket userSocket;

        private Connection(Socket userSocket) {
            this.userSocket = userSocket;
        }

        @Override
        public void run() {

        }
    }
    private class Room implements Runnable {

        private static int totalRoomsCreated = 0;
        private final static Map<Integer, Room> runningRooms = new HashMap<>();

        private final int id;
        private final Socket hostSocket;

        // player name + socket
        private final Map<String, Socket> playerSockets = new HashMap<>();

        public Room(Socket hostSocket) {
            this.id = totalRoomsCreated;
            totalRoomsCreated++;

            this.hostSocket = hostSocket;
        }


        @Override
        public void run() {
            runningRooms.put(this.id, this);
            playerSockets.put("host", hostSocket);
        }
    }
}
