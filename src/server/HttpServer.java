package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    public static void main(String[] args) {
        int argNumber = 2;
        int logArgNumber = 4;
        int portNumber;
        final String usage = "arguments: <port number> <root name> <-r | -R> <log path>";
        ConcreteLogCenter logCenter;
        FileLog fileLog = new FileLog();

        // If user inputs the wrong number of arguments then output the usage.
        if (args.length != argNumber && args.length != logArgNumber) {
            System.err.println(usage);
            System.exit(-1);
        }

        portNumber = Integer.parseInt(args[0]);
        HTTPFileServer.setRootDir(args[1]);
        logCenter = ConcreteLogCenter.getInstance();
        logCenter.addObserver(fileLog);

        if (args.length == logArgNumber && args[2] != null) {
            fileLog.setLogRoot(args[3]);
            switch (args[2]) {
                case "-r":
                    ConcreteLogCenter.setLogMode(true);
                    logCenter.startLog();
                    break;

                case "-R":
                    fileLog.emptyFile();
                    ConcreteLogCenter.setLogMode(true);
                    logCenter.startLog();
                    break;
                default:
                    System.err.println(usage);
                    System.exit(-1);
            }
        }

        try {
            @SuppressWarnings("resource")

            // Run the server
                    ServerSocket serverSocket = new ServerSocket(portNumber);

            // Always listen for the new client. Use thread for multiple clients.
            while (true) {
                Socket client = serverSocket.accept();
                Thread threadClient = new MultiClient(client);
                threadClient.start();
            }

        } catch (IOException e) {
            System.err.println("Connection error, please check the port number.");
            System.exit(-1);
        }
    }
}
