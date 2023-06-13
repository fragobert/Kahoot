package comv.kahoot.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Client {
    private static final String COMMAND_PREFIX = "/";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT_NUMBER = 6000;

    /**
     * regex pattern that only permits letters and numbers.
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]*$");

    public void start() {
        try (Socket clientSocket = new Socket(IP_ADDRESS, PORT_NUMBER);
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String username = readUsername();
            char option = readOption();

            switch (option) {
                case 'h' -> {
                    out.writeUTF("h" + username);
                }
                case 'j' -> {
                    String roomNumber = readRoomNumber();
                    out.writeUTF("j" + roomNumber + username);
                }
                default -> System.out.println("Wrong Input");
            }


            Thread messageReaderThread = new Thread(() -> readServerResponses(in, out, option));
            messageReaderThread.start();

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.startsWith(COMMAND_PREFIX)) {
                    handleCommand(userInput.substring(1));
                } else {
                    out.writeUTF(username + ": " + userInput);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + IP_ADDRESS);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Server probably isn't running on " + IP_ADDRESS);
            System.exit(1);
        }
    }


    /**
     * Reads and displays server responses.
     *
     * @param in the reader to read server responses from
     */
    private void readServerResponses(DataInputStream in, DataOutputStream out, char userType) {
        try {
            String roomId = "";
            String serverResponse = in.readUTF();

            if(userType == 'h'){
                System.out.println("Room number: " + serverResponse.substring(1));
                serverResponse = "esuccess";
                System.out.println("Press [Enter] to start");

                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                reader.readLine();
                out.writeUTF("r");
            }

            if (serverResponse.startsWith("h0000")) { //TODO: ROOM NUMBER CAN BE 0000
                switch (serverResponse) {
                    case "h00000" -> System.err.println("Invalid username");
                    case "h00001" -> System.err.println("Too many rooms running");
                    case "h00002" -> System.err.println("Room already started");
                    case "h00009" -> System.err.println("Unknown Error");
                }
            } else if (serverResponse.startsWith("efailure")) {
                System.err.println("Error");
            } else if (serverResponse.startsWith("esuccess")) {
                String received = null;
                String state = "q";
                while (!(received = in.readUTF()).equals("f")) {
                    switch (state) {
                        case "q" -> {
                            out.writeUTF(readQuestion(received));
                            state = "c";
                        }
                        case "c" -> {
                            //TODO: VISUELL RICHTIGE ANTWORTEN DARSTELLEN
                            System.out.println(received);
                            state = "p";
                        }
                        case "p" -> {
                            //TODO: VISUELL RICHTIGE ANTWORTEN DARSTELLEN
                            System.out.println(received);
                            state = "q";
                        }
                    }
                }
                System.out.println("Room ended");
            }
        } catch (IOException e) {
            System.err.println("Error reading server response: " + e.getMessage());
        }
    }

    private String readQuestion(String received) throws IOException {
        String[] qAndA = received.split(";");
        qAndA[0] = qAndA[0].substring(1);

        System.out.println("Question: " + qAndA[0]);

        for (int i = 1; i < qAndA.length; i++) {
            System.out.println("Answer " + i + ": " + qAndA[i]);
        }

        BufferedReader answerScanner = new BufferedReader(new InputStreamReader(System.in));

        String answers = answerScanner.readLine();

        StringBuilder sb = new StringBuilder("a");
        sb.append(qAndA[0]);
        sb.append(";0101");
        // Regex: a<some question>;<answersEncoded>
        System.out.println(sb.toString());
        return sb.toString();

    }


    /**
     * Reads the username from the user.
     *
     * @return the username entered by the user
     * @throws IOException if an I/O error occurs while reading the input
     */
    private String readUsername() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String username;
        do {
            System.out.println("Enter your username");
            username = reader.readLine();
        } while (!validateUsername(username));
        return username;
    }

    /**
     * Validates the username for length and special characters.
     *
     * @param username the username to validate
     * @return true if the username is valid, false otherwise
     */
    private boolean validateUsername(String username) {
        if (username.length() < 5 || username.length() > 20) {
            System.err.println("The username must have between 5 and 20 characters. ");
            return false;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            System.err.println("The username can't contain any special characters. ");
            return false;
        }
        return true;
    }

    /**
     * Reads the option from the user.
     *
     * @return the option entered by the user
     * @throws IOException if an I/O error occurs while reading the input
     */
    private char readOption() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String answer;
        char option;
        do {
            System.out.println("Do you want to join or host?");
            answer = reader.readLine();
            option = answer.toLowerCase().charAt(0);
        } while (!validateOption(option));
        return option;
    }

    /**
     * Validates the option for letter.
     *
     * @param option to validate
     * @return true if the option is valid, false otherwise
     */
    private boolean validateOption(char option) {
        return option == 'j' || option == 'h';
    }


    /**
     * Reads the roomnumber from the user.
     *
     * @return the roomnumber entered by the user
     * @throws IOException if an I/O error occurs while reading the input
     */
    private String readRoomNumber() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String roomNumber;
        do {
            System.out.println("Enter your roomnumber");
            roomNumber = reader.readLine();
        } while (!validateRoomNumber(roomNumber));
        return roomNumber;
    }

    /**
     * Validates the roomnumber for length.
     *
     * @param roomNumber to validate
     * @return true if the option is valid, false otherwise
     */
    private boolean validateRoomNumber(String roomNumber) {
        return roomNumber.length() == 4 && !roomNumber.equals("0000");
    }

    /**
     * Handles a command received from the user.
     *
     * @param command the command to handle
     */
    private void handleCommand(String command) {
        switch (command) {
            case "exit" -> {
                System.out.println("Connection is being aborted...");
                System.exit(0);
            }
            default -> System.out.println("Unknown command: " + "\"" + command + "\"");
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}