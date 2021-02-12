package PServer;

import java.util.StringTokenizer;

public class ProxyProtocol {

    public boolean isItLocal(String input) {
        // This function checks if the request is sent to localhost or IP of it
        // It returns true if request is sent to local server, false otherwise
        if(input == null)
            return false;

        return (input.contains("localhost") || isItIpAddress(input.split(" ")[1])) && !input.contains("favicon");
    }

    public String[] toServer(String input) {
        //This functions prepares the message that will be sent to the server
        // Input : String that server thread received by the proxy server
        // Output : String array that consists hostname, port number, and message
        // Zeroth index can be null of "", if it's null that means size is larger than 9999, "" otherwise
        // First index is the hostname or IP
        // Second index is the port number
        // Third index is the message that will be sent to the server

        StringTokenizer tokens = new StringTokenizer(input); //This function separates the input by " "
        String method = tokens.nextToken(); //First one will method of the request ie. GET
        String url = tokens.nextToken(); //URL part will be the next token
        String []urlSplit = url.split("/"); // We split it by the "/"
        String[] output = new String[4];

        // We check if the url has localhost in it, or its the IP of the server
        if(urlSplit.length == 4 && (isItLocal(urlSplit[2]) || urlSplit[2].split("\\.").length == 4)) {
            output[0] = ""; // output is "" normal case

            // If size is greater than 9999 output becomes null
            //, that means we will send error message to client
            if(Integer.parseInt(urlSplit[3]) > 9999)
                output[0] = null;
            // normal format is http://localhost:8080/500,
            // when we parse it by "/" first one : http, second "", third localhost:portnumber, fourth  size
            output[1] = urlSplit[2].split(":")[0];
            output[2] = urlSplit[2].split(":")[1];
            output[3] = method + " /" + urlSplit[3] + " " + tokens.nextToken();
        }
        else if(urlSplit.length == 2 && urlSplit[0].compareTo("") == 0){
            //This one is the special case for GET /500 HTTP/1.0
            //We automatically send it to out local web server
            output[0] = "";
            if(Integer.parseInt(urlSplit[1]) > 9999) // We check if the size is appropriate
                output[0] = null;
            output[1] = "localhost";
            output[2] = "8080";
            output[3] = method + " /" + urlSplit[1] + " " + tokens.nextToken();
        }

        return output;
    }

    public boolean isItIpAddress(String input) {
        String url = input.split(":")[0];
        if(!(url.split("\\.").length == 4))
            return false;

        int length = url.length();
        for (int i = 0; i < length; i++) {
            if ((url.charAt(i) < '0' || url.charAt(i) > '9') && url.charAt(i) != '.')
                return false;
        }

        return true;
    }
}