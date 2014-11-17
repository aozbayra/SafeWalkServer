import java.io.*;
import java.net.*;
import java.util.*;

public class SafeWalkServer implements Runnable {
    protected Socket socket;
    private ServerSocket serverSocket;
    protected Client client;
    protected ArrayList<Client> clientList = new ArrayList<Client>();
    
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
        int commandCheck = -1;
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
                        int matchIndex = checkMatch();
                        if ( matchIndex > -1) 
                            giveResponse(matchIndex);
                        else {
                            clientList.add(client);
                            System.out.println("No match is found");
                        }
                    }
                    else {
                        printError();
                    }
                    
                }
                else {
                    commandCheck = checkValidityCommand(input);
                    if (commandCheck == 0) 
                        printError();
                    else if (commandCheck == 1)
                        listRequests();
                    else if (commandCheck == 2)
                        serverReset();
                    else if (commandCheck == 3) {
                        serverShutdown();
                        break;
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
    
    private void printError() throws IOException {
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("ERROR: invalid request");
        pw.flush();
        socket.close();
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
    
    private int checkValidityCommand(String input) {
        if (input.equals(":LIST_PENDING_REQUESTS")) 
            return 1;
        else if (input.equals(":RESET")) 
            return 2; 
        else if (input.equals(":SHUTDOWN")) 
            return 3;
        return 0; 
    }
    
    private void listRequests() throws IOException {
        int count = 0;
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        
        if (clientList.size() != 0) {
            pw.print("[");
            pw.flush();
            for (Client i : clientList) {
                pw.print("[" + i.name + ", " + i.from + ", " + i.to + ", " + i.type);
                pw.flush();
                
                if (count != clientList.size() - 1) {
                    pw.print("], ");
                    pw.flush();
                }
                count++;
            }
            pw.print("]]");
            pw.flush();
        }
    }
    
    public void serverReset() throws IOException {
        PrintWriter clientReset = new PrintWriter(socket.getOutputStream());
        clientReset.println("RESPONSE: success");
        clientReset.flush();
        
        for (Client i : clientList) {
            PrintWriter pw = new PrintWriter(i.getOutputStream());
            pw.println("ERROR: connection reset");
            pw.flush();
            i.socket.close();
        }
        clientReset.close();
    }
    
    private void serverShutdown() throws IOException {
        for (Client i : clientList) {
            i.socket.close();
        }
        socket.close();
    }
    
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
    
    public int checkMatch() {
        String fromCurrent = client.from;
        String toCurrent = client.to; 
        for(Client i: clientList) {
            if (i.from.equals(fromCurrent)) {
                if (i.to.equals(toCurrent) || i.to.equals("*") || toCurrent.equals("*")) {
                    if (i.to.equals("*") && toCurrent.equals("*"))
                        continue;
                    else 
                        return clientList.indexOf(i);
                }
            }
        }
        return -1;
    }
    
    public void giveResponse(int matchIndex) throws IOException {
        Client pairedClient = clientList.get(matchIndex);
        PrintWriter current = new PrintWriter(client.getOutputStream());
        PrintWriter paired = new PrintWriter(pairedClient.getOutputStream());
        current.println("RESPONSE: " + pairedClient.name + "," + pairedClient.from + "," 
                            + pairedClient.to + "," + pairedClient.type);
        current.flush();
        paired.println("RESPONSE: " + client.name + "," + client.from + ","
                           + client.to + "," + client.type);
        paired.flush();
        clientList.remove(matchIndex);
        current.close();
        paired.close();
        client.socket.close();
        pairedClient.socket.close();
        
        
    }
    
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
        
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }            
    }
    
}