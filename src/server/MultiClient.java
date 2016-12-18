package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * The each client is run in the thread to support the multiple clients.
 */
public class MultiClient extends Thread {
    public static final String ERR_IO_ERROR = "Unable to read.";
    private static final String HTTP_CRLF = "\r\n";
    private Socket client;
    private RequestHandler requestHandler;
    private BufferedReader input;
    private OutputStream output;

    public MultiClient(Socket client) {
        this.client = client;
        requestHandler = new RequestHandler();
    }

    public void run() {
        byte[] response;
        String request = "";
        String eachLine = "";
        byte[] entity = null;

        try {
            input = new BufferedReader(new InputStreamReader(
                    client.getInputStream()));
            output = client.getOutputStream();

            // Read the whole request.
            while ((eachLine = input.readLine()) != null && !eachLine.equals("")) {
                request += eachLine;
                request += HTTP_CRLF;
            }

            // If the method is post, append the entity.
            if ((entity = getEntity(request, input)) != null) {
                requestHandler.setEntity(entity);
            }

// 			System.out.println(request);
            response = requestHandler.processRequest(request
                    .getBytes("US-ASCII"));
//			System.out.println(new String (response));
            output.write(response);
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            System.err.println(ERR_IO_ERROR);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.err.println(ERR_IO_ERROR);
            }
        }
    }

    // Get the content size for reading the entity of the request.
    private int getContentSize(String request) {
        int length = -1;
        String[] lines = request.split(HTTP_CRLF);
        Pattern patternLength = Pattern.compile("\\d+");

        for (String line : lines) {
            if (line.contains("Content-Length")) {
                Matcher mather = patternLength.matcher(line);
                while (mather.find()) {
                    length = Integer.parseInt(mather.group(0));
                }
                break;
            }
        }
        return length;
    }

    // Get entity of the request.
    private byte[] getEntity(String request, BufferedReader input) {
        int validLength = 0;
        int length = getContentSize(request);
        byte[] entity = null;

        if (length >= validLength) {
            try {
                entity = new byte[length];
                for (int index = 0; index < length; index++) {
                    entity[index] = (byte) input.read();
                }
            } catch (IOException e) {
                System.err.println(ERR_IO_ERROR);
            }
        }
        return entity;
    }
}
