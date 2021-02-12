package PServer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class ProxyThread extends Thread {
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private Socket clientSocket;

    private PrintWriter serverOut;
    private BufferedReader serverIn;
    private Socket serverSocket;

    public ProxyThread(Socket clientSocket) {
        // We open our IO connections to the client
        this.clientSocket = clientSocket;
        try {
            this.clientOut = new PrintWriter(clientSocket.getOutputStream());
            this.clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error occurred while trying to open IO connections to client!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        ProxyProtocol protocol = new ProxyProtocol();
        String request = null;
        try {// We receive the first line of the message
            request = clientIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while trying to read request of client!");
        }
        // We check if request sent to our server, if its not we return after closing connections.
        if (!protocol.isItLocal(request)) {
            try {
                clientOut.close();
                clientIn.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // We send request to our protocol for processing
        String[] forServer = protocol.toServer(request);
        System.out.println(request);//We print request

        //If input is not appropriate we do not connect to the server.
        //We send error message to client and close the connection.
        if (forServer[0] == null) {
            System.out.println("URI too long Error!");
            requestUriTooLongMessage();

            try {
                clientOut.close();
                clientIn.close();
                clientSocket.close();
                System.out.println("Connections closed!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (isItCached(forServer[3]) && conditionalGet(forServer[3])) {
            System.out.println("Request is cached! Sending it to the client!");
            //We create reader for file
            String fileName = createFileName(forServer[3]);
            BufferedReader fileIn = null;
            try {
                File file = new File(fileName);
                fileIn = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                System.out.println("Problem occurred while trying to open reader for " + fileName);
                e.printStackTrace();
            }
            //We read the file and send it to the client
            try {
                String input;
                while ((input = fileIn.readLine()) != null) {
                    if (!(input.compareTo("</html>") == 0))
                        input += "\n";
                    clientOut.print(input);
                    clientOut.flush();
                }
            } catch (IOException e) {
                System.out.println("Problem occurred while trying to reading from " + fileName + " and sending it to the client!");
                e.printStackTrace();
            }

            //After finishing reading from file and sending its content to client,
            //we close the connection.
            try {
                fileIn.close();
                clientOut.close();
                clientIn.close();
                clientSocket.close();
                System.out.println("Connections closed!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //If its not cached or, its a even sized request we connect with server.
            connectToServer(forServer);
        }
    }

    private void connectToServer(String[] forServer) {
        //For opening connection with server
        if(!openConnections(forServer))
            return;

        //For reading request of the client, and sending them to server
        String input = forServer[3];
        while (input != null) {
            serverOut.println(input);
            try {
                input = clientIn.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error occurred while trying to read request of client!");
            }
            if (input.equals(""))
                break;
        }
        serverOut.flush();

        //For sending response of the server to the client, and caching them to a file
        try {
            String fileName = createFileName(forServer[3]);
            File file = new File(fileName);
            file.createNewFile();
            PrintWriter fileOut = new PrintWriter(file);
            while ((input = serverIn.readLine()) != null) {
                if (!(input.compareTo("</html>") == 0))
                    input += "\n";
                clientOut.print(input);
                fileOut.print(input);
                clientOut.flush();
                fileOut.flush();
            }

            fileOut.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while trying to read response from server!");
        }

        //For closing IO streams and sockets
        try {
            serverOut.close();
            serverIn.close();
            clientOut.close();
            clientIn.close();

            serverSocket.close();
            clientSocket.close();
            System.out.println("Connections closed!");
        } catch (IOException e) {
            System.out.println("Error occurred while trying to close the connections!");
            e.printStackTrace();
        }

    }

    private boolean openConnections(String[] forServer) {
        //For opening the connection to the server
        try {
            this.serverSocket = new Socket(forServer[1], Integer.parseInt(forServer[2]));
        } catch (UnknownHostException e) {
            notFoundMessage();
            System.out.println("Error occurred while trying to connect socket!");
            return false;
        } catch (IOException e) {
            notFoundMessage();
            System.out.println("Error occurred while trying to connect socket!");
            return false;
        }

        //Opening IO streams of the server
        try {
            this.serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            this.serverOut = new PrintWriter(serverSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error occurred while trying to open in/out streams!");
            return false;
        }

        return true;
    }

    private void notFoundMessage() {
        // For sending the error of server is not active
        String message = "HTTP/1.1 404 Not Found\n" +
                "Content-Type: text/html\n" +
                "Content-Length: 52\n" +
                "\r\n" +
                "<html>\n" +
                "<body>\n" +
                "<h1>"+ "404 Not Found" + "</h1>\n" +
                "</body>\n" +
                "</html>";

        clientOut.write(message);
        clientOut.flush();

        try {
            clientOut.close();
            clientIn.close();

            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error occurred while closing the connetion to the client!");
            e.printStackTrace();
        }
    }

    private void requestUriTooLongMessage() {
        String message = "HTTP/1.1 414 URI Too Long\n" +
                "Content-Type: text/html\n" +
                "Content-Length: 55\n" +
                "\r\n" +
                "<html>\n" +
                "<body>\n" +
                "<h1>"+ "414 URI Too Long" + "</h1>\n" +
                "</body>\n" +
                "</html>";

        clientOut.write(message);
        clientOut.flush();

    }

    private boolean isItCached(String request) {
        //This function checks if the request is cached before
        File file = new File(createFileName(request));

        if (!file.exists()) {
            System.out.println(request + " is not cached!");
            return false;
        } else
            return true;
    }

    private String createFileName(String request) {
        //This function generates special file name for given request
        StringTokenizer tokens = new StringTokenizer(request);
        return tokens.nextToken() + tokens.nextToken().split("/")[1] + ".txt";
    }

    private boolean conditionalGet(String request) {
        // This one checks if the requested file size is even or odd
        // if it's even it deletes the cached file, than it returns false
        // if it's odd it returns true
        StringTokenizer tokens = new StringTokenizer(request);
        tokens.nextToken();
        String size = tokens.nextToken().split("/")[1];

        if (Integer.parseInt(size) % 2 == 0) {
            System.out.println(request +  " is modified!");
            String fileName = createFileName(request);
            File file = new File(fileName);
            file.delete();
            return false;
        } else
            return true;
    }
}
