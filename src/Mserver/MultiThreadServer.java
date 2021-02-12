package Mserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadServer {

    public static void main(String[] args) { // Takes one argument for port number
        if (args.length != 1) {
            System.err.println("Usage: java Mserver.MultiThreadServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNumber); // Creates the server socket with given argument
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Waits for client
                ServerThread t = new ServerThread(clientSocket); // Creates a thread for communicate with a client
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
