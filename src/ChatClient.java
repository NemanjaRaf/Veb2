import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class ChatClient {
    BufferedReader in;
    PrintWriter out;
    BufferedReader userInput;

    public ChatClient() {
        userInput = new BufferedReader(new InputStreamReader(System.in));
    }

    private String getServerAddress() throws IOException {
        return "localhost";
    }

    private String getName() throws IOException {
        System.out.println("Upisi ime:");
        return userInput.readLine();
    }

    private void run() throws IOException {
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 1234);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                System.out.println("Uspesno ste se ulogovali");
                new Thread(new UserInputHandler()).start();
            } else if (line.startsWith("MESSAGE")) {
                System.out.println(line.substring(8));
            }
        }
    }

    private class UserInputHandler implements Runnable {
        public void run() {
            try {
                while (true) {
                    String message = userInput.readLine();
                    if (message != null) {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling user input: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.run();
    }
}