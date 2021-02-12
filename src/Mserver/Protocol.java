package Mserver;

import java.util.StringTokenizer;

public class Protocol {
    /* In this class we need to precess the input and generate a output.*/

    public String[] inputProcessing(String input) {
        // Input : String that server thread received by the server
        // Output : Array of Strings which is a size of 5
        // Zeroth index will be the status line of response
        // First index will be header for content type
        // Second index will be header for content length
        // Third index will be an empty line to separate headers from data
        // Fourth index will be html data

        if (input == null)
            return null;
        StringTokenizer tokens = new StringTokenizer(input); //This function separates the input by " "
        String method = tokens.nextToken(); //First one will method of the request ie. GET
        String[] answer = new String[5];


        switch (method) {
            case "GET":
                String sizeIn = tokens.nextToken().substring(1); // Second toke will be like /size
                // When we get substring of it we will be left with size
                if (!isItAnInteger(sizeIn) || Integer.parseInt(sizeIn) > 20000 || Integer.parseInt(sizeIn) < 100) {
                    // We control if the size is an integer and larger than 100 and smaller than 20000

                    // Bad request message
                    System.out.println("Bad Request is generated for " + input);
                    answer[0] = "HTTP/1.1 400 Bad Request\n";
                    answer[1] = "Content-Type: text/html\n";
                    answer[2] = "Content-Length: 54\n";
                    answer[3] = "\r\n";
                    answer[4] = "<html>\n" +
                            "<body>\n" +
                            "<h1>"+ "400 Bad Request" + "</h1>\n" +
                            "</body>\n" +
                            "</html>";
                }
                else {
                    //If the message is in the format that we accept
                    int size = Integer.parseInt(sizeIn);
                    System.out.println("html with size of " + size + " created for " + input);
                    answer[0] = "HTTP/1.1 200 OK\n";
                    answer[1] = "Content-Type: text/html\n";
                    answer[3] = "\r\n";
                    answer[4] = HTMLGenerator(size);
                    answer[2] = "Content-Length: " + answer[4].length() + "\n";
                }

                break;
            case "OPTIONS":
            case "HEAD":
            case "POST":
            case "PUT":
            case "DELETE":
            case "TRACE":
            case "CONNECT":
                // Not implemented error
                System.out.println("Not Implemented is generated for " + input);
                answer[0] = "HTTP/1.1 501 Not Implemented\n";
                answer[1] = "Content-Type: text/html\n";
                answer[2] = "Content-Length: 58\n";
                answer[3] = "\r\n";
                answer[4] = "<html>\n" +
                        "<body>\n" +
                        "<h1>"+ "501 Not Implemented" + "</h1>\n" +
                        "</body>\n" +
                        "</html>";
                break;
            default:
                //For unknown methods
                // Bad request error
                System.out.println("Bad Request is generated for " + input);
                answer[0] = "HTTP/1.1 400 Bad Request\n";
                answer[1] = "Content-Type: text/html\n";
                answer[2] = "Content-Length: 54\n";
                answer[3] = "\r\n";
                answer[4] = "<html>\n" +
                        "<body>\n" +
                        "<h1>"+ "400 Bad Request" + "</h1>\n" +
                        "</body>\n" +
                        "</html>";
                break;
        }
        return answer;
    }

    private boolean isItAnInteger(String input) {
        //This function is used for checking if an string consist only numbers

        int length = input.length();
        for(int i = 0; i < length; i++) {
            if (input.charAt(i) < '0' || input.charAt(i) > '9')
                return false;
        }

        return true;
    }

    private String HTMLGenerator(int size) { // We generate a html file with given size
        String content = "";
        for (int i = 0; i < size - 39; i++)
            content += "a";
        // parts except content parts is size of 39byte, so we create content variable to get desired size
        String html = "<html>\n" +
                "<body>\n" +
                "<h1>"+ content + "</h1>\n" +
                "</body>\n" +
                "</html>";

        return html;
    }

}
