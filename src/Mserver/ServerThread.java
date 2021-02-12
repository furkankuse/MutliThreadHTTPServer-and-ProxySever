package Mserver;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread{

    private PrintWriter out;
    private BufferedReader in;
    private Socket clientSocket;

    public ServerThread(Socket clientSocket) {
        System.out.println("Connected!");
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream());
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch (IOException e) {
            System.out.println("Problem occurred when we try to create IO streams!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Protocol protocol = new Protocol();
        try {
            String input = in.readLine();
            System.out.println(input + " is received!");
            String[] answer = protocol.inputProcessing(input);
            if (answer!= null) {
                for(int i = 0; i < answer.length; i ++) {
                    out.print(answer[i]);
                }
            }
            out.flush();
            out.close();
            in.close();
        } catch (IOException e) {
            System.out.println("Problem occurred when we try to receive a message and return its response to client!");
            e.printStackTrace();
        }

        try {
            clientSocket.close();
            System.out.println("Connection closed!");
        } catch (IOException e) {
            System.out.println("Error occurred while closing the connection with the client!");
            e.printStackTrace();
        }
    }
}
