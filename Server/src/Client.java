import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Entrez l'adresse IP du serveur : ");
            String serverAddress = scanner.nextLine();
            System.out.print("Entrez le port du serveur (5000-5050) : ");
            int port = Integer.parseInt(scanner.nextLine());
            System.out.print("Entrez votre nom d'utilisateur : ");
            String username = scanner.nextLine();
            System.out.print("Entrez votre mot de passe : ");
            String password = scanner.nextLine();

            try (Socket socket = new Socket(serverAddress, port)) {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                // Envoi des informations de connexion
                out.writeUTF(username);
                out.writeUTF(password);

                // Lecture de la réponse du serveur
                System.out.println("Serveur : " + in.readUTF());

                // Lecture des messages en temps réel
                Thread readThread = new Thread(() -> {
                    try {
                        while (true) {
                            String message = in.readUTF();
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        System.out.println("Déconnecté du serveur.");
                    }
                });
                readThread.start();

                // Envoi des messages au serveur
                while (true) {
                    String message = scanner.nextLine();
                    out.writeUTF(message);
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur client : " + e.getMessage());
        }
    }
}
