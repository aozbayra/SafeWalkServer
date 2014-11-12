import java.io.*;
import java.net.*;
import java.util.*;

public class SafeWalkServer implements Runnable {
    private Socket clientSocket;
    private ServerSocket serverSocket;
    
    public static void main(String[] args) {
        if (args.length == 0) {
            try {
                new SafeWalkServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (args.length == 1) {
            try {
                int port = Integer.parseInt(args[0]); // Get the given port number
                // Check if it is in the given range
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
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
    }
    
    public SafeWalkServer() throws IOException {
        serverSocket = new ServerSocket(0);
        int port = getLocalPort();
        if (port > 1025 && port < 65535) {
            System.out.println("Port not specified. Using free port " + port);
            serverSocket.setReuseAddress(true);
            run();
        } else
            new SafeWalkServer(); // If the available port number is not in the given range re-search.
    }
    
    public void run() {
        while (true) {
            try {
            clientSocket = serverSocket.accept();
            send();
            receive();
            // TODO
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }
    
    public void send() {
        try {
            PrintWriter netOut = new PrintWriter(clientSocket.getOutputStream());  // Tool to send input to the server socket
            Scanner in = new Scanner(System.in);  // Tool to get input from keyboard
            System.out.println("Send something: ");
            String input = in.nextLine();
            netOut.println(input);  // Writing to the server
            netOut.flush(); // Sending what I just wrote
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void receive() {
        try {
            Scanner netBuffer = new Scanner(clientSocket.getInputStream());
            System.out.println(netBuffer.nextLine());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
            
            
    
    
    
}
        
            