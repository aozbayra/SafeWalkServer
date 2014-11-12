import java.io.*;
import java.net.*;
import java.util.*;

public class SafeWalkServer implements Runnable {
    private Socket socket;
    
    public static void main(String[] args) {
        if (args.length == 0) {
            try {
                new SafeWalkServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (args.length == 1) {
            try {
                int port = Integer.parseInt(args[0]);
                if (port > 1025 && port < 65535)
                    new SafeWalkServer(Integer.parseInt(args[0]));
                else
                    System.out.println("Invalid port number");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("Command not found");
    }
    
    public SafeWalkServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();
        socket.setReuseAddress(true);
    }
    
    public SafeWalkServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        if (port > 1025 && port < 65535) {
            System.out.println("Port not specified. Using free port " + port);
            socket = serverSocket.accept();
            socket.setReuseAddress(true);
        } else
            new SafeWalkServer();
    }
    
    
    
}
        
            