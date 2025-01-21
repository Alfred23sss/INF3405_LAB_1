import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int MIN_PORT = 5000;
    private static final int MAX_PORT = 5050;
    private static Map<String, String> users = new HashMap<>(); // Nom d'utilisateur -> Mot de passe
    private static List<String> messages = new ArrayList<>();  // Historique des messages
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Entrez l'adresse IP du serveur : ");
            String serverAddress = scanner.nextLine();
            System.out.print("Entrez le port d'écoute (5000-5050) : ");
            int port = Integer.parseInt(scanner.nextLine());

            if (!isValidIP(serverAddress) || port < MIN_PORT || port > MAX_PORT) {
                System.out.println("Adresse IP ou port invalide.");
                return;
            }

            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(serverAddress, port));
            System.out.printf("Serveur lancé sur %s:%d%n", serverAddress, port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (Exception e) {
            System.out.println("Erreur serveur : " + e.getMessage());
        }
    }

    private static boolean isValidIP(String ip) {
        return ip.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
            ) {
                // Lecture des informations utilisateur
                String username = in.readUTF();
                String password = in.readUTF();

                synchronized (users) {
                    if (!users.containsKey(username)) {
                        users.put(username, password);
                        out.writeUTF("Compte créé avec succès !");
                    } else if (!users.get(username).equals(password)) {
                        out.writeUTF("Erreur : Mot de passe incorrect.");
                        return;
                    } else {
                        out.writeUTF("Connexion réussie !");
                    }
                }

                // Envoi des 15 derniers messages
                synchronized (messages) {
                    for (String message : messages.subList(Math.max(0, messages.size() - 15), messages.size())) {
                        out.writeUTF(message);
                    }
                }

                // Gestion des messages en temps réel
                while (true) {
                    String clientMessage = in.readUTF();
                    String formattedMessage = String.format("[%s - %s:%d]: %s",
                            username, clientSocket.getInetAddress(), clientSocket.getPort(), clientMessage);
                    System.out.println(formattedMessage);

                    synchronized (messages) {
                        messages.add(formattedMessage);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client déconnecté.");
            }
        }
    }
}
