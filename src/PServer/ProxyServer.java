package PServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {

    public static void main(String[] args) {

        int portNumber = 8888;
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(portNumber); // Creates the server socket with given agument
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Waits for client
                ProxyThread t = new ProxyThread(clientSocket);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
