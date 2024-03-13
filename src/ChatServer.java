import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class ChatServer {
    private static final int PORT = 1234;
    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static LinkedList<String> messageHistory = new LinkedList<String>();
    private static final int MAX_HISTORY = 100;
    private static final List<String> CENSORED_WORDS = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        setupBadWords();

        System.out.println("Chat server jer pokrenut");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static void setupBadWords() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("badwords.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                CENSORED_WORDS.add(line);
            }
        } catch (IOException e) {
            System.out.println("Greska pri ucitavanju ruznih reci: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    System.out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + name);
                System.out.println("NAMEACCEPTED " + name);
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE Tisina, dosao je " + name);
                }
                writers.add(out);

                for (String message : messageHistory) {
                    out.println("MESSAGE " + message);
                }

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    for (String word : CENSORED_WORDS) {
                        input = input.replaceAll("\\b" + word + "\\b", censorWord(word));
                    }
                    String message = new SimpleDateFormat("HH:mm:ss").format(new Date()) + " - " + name + ": " + input;
                    if (messageHistory.size() == MAX_HISTORY) {
                        messageHistory.removeFirst();
                    }
                    messageHistory.addLast(message);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private String censorWord(String word) {
            if (word.length() <= 2) {
                return word;
            }
            char[] chars = word.toCharArray();
            Arrays.fill(chars, 1, chars.length - 1, '*');
            return new String(chars);
        }
    }
}