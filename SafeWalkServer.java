import java.io.*;
import java.net.*;
import java.util.*;

public class SafeWalkServer implements Runnable {
    protected Socket socket;
    private ServerSocket serverSocket;
    protected Client client;
    List<Client> clientList = new ArrayList<Client>();
    
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
                if (port > 1025 && port < 65535) {
                    new SafeWalkServer(Integer.parseInt(args[0]));
                }
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
        run();
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
                socket = serverSocket.accept();
                System.out.printf("Connection received from %s%n", socket);
                String input = getInput();
                System.out.println("Received from client: " + input);
                if (!isCommand(input)) {
                    if (checkValidityRequest(input)) {
                        String[] tokens = extractTokens(input);
                        client = new Client(socket, tokens);
                        
                        clientList.add(client);

                        
                        

                    }
                }
                
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }
    
    public String getInput() throws Exception {
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw.println("Enter a command or request: ");
        pw.flush();
        String input = in.readLine();
        return input;
    }
    
    private boolean isCommand(String input) {
        if (input.charAt(0) == ':') 
            return true;
        return false;
    }
    
//    private void checkValidityCommand(String input) {
//        if (input.equals(":LIST_PENDING_REQUESTS"))
//            // CALL THE METHOD
//        else if (input.equals(":RESET"))
//            // CALL THE METHOD
//        else if (input.equals(":SHUTDOWN"))
//            // Close the calling client and also any other open client
//        
//        else
//            return false;
//    }
    
    public boolean checkValidityRequest(String input) {
        String[] locations = {"CL50", "EE", "LWSN", "PMU", "PUSH", "*"};
        List<String> locList = new ArrayList<String>(Arrays.asList(locations));
        
        String[] tokens = extractTokens(input);

        if (tokens.length == 1) 
            return false;
        if (!locList.contains(tokens[2]))
            return false;
        if (!locList.contains(tokens[1]))
            return false;
        if (tokens[2].equals(tokens[1]))
            return false;
        if (tokens[1].equals("*"))
            return false;
   
        return true;
    }
    
    public int checkFrom(Client client, ArrayList list) {
        String fromCurrent = client.from;
        for (int i = 0;i < list.size(); i++) {
            
            
    

        
        
    
    private String[] extractTokens(String input) {
        char[] charArray = input.toCharArray();
        int[] commaIndex = new int[3];
        int c = 0;
        String[] error = {"Error"};
        
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == ',') {
                commaIndex[c] = i;
                c++;
            }
        }
        
        if (c != 3) 
            return error;
        
        String name = input.substring(0, commaIndex[0]);
        String from = input.substring(commaIndex[0] + 1, commaIndex[1]);
        String to = input.substring(commaIndex[1] + 1, commaIndex[2]);
        String type = input.substring(commaIndex[2] + 1, input.length());
        String[] tokens = {name, from, to, type};
        
        return tokens;
    }
    
    private class Client {
        String name;
        String from;
        String to;
        String type;
        Socket socket;
        Client(Socket socket, String[] tokens) throws IOException {
            this.socket = socket;
            this.name = tokens[0];
            this.from = tokens[1];
            this.to = tokens[2];
            this.type = tokens[3];
        }
        
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }
        
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }
            
    }
        
      
            
    
}