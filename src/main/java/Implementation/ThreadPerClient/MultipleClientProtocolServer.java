package Implementation.ThreadPerClient;

import Implementation.TBGP_Protocol.TBGP_ProtocolFactory;
import Interfaces.ServerProtocolFactory;

import java.io.IOException;
import java.net.ServerSocket;

class MultipleClientProtocolServer implements Runnable {
    private ServerSocket serverSocket;
    private int listenPort;
    private ServerProtocolFactory factory;

    public MultipleClientProtocolServer(int port, ServerProtocolFactory p)
    {
        serverSocket = null;
        listenPort = port;
        factory = p;
    }

    public void run()
    {
        try {
            serverSocket = new ServerSocket(listenPort);
            System.out.println("Listening...");
        }
        catch (IOException e) {
            System.out.println("Cannot listen on port " + listenPort);
        }

        while (true)
        {
            try {
                ConnectionHandler newConnection = new ConnectionHandler(serverSocket.accept(), factory.create());
                new Thread(newConnection).start();
            }
            catch (IOException e)
            {
                System.out.println("Failed to accept on port " + listenPort);
            }
        }
    }


    // Closes the connection
    public void close() throws IOException
    {
        serverSocket.close();
    }

    public static void main(String[] args) throws IOException
    {
        // Get port
        int port = Integer.decode(args[0]).intValue();

        MultipleClientProtocolServer server = new MultipleClientProtocolServer(port, new TBGP_ProtocolFactory());
        Thread serverThread = new Thread(server);
        serverThread.start();
        try {
            serverThread.join();
        }
        catch (InterruptedException e)
        {
            System.out.println("Server stopped");
        }
    }
}





























/*import java.io.*;
import java.net.*;
 
class Server {
    
    private BufferedReader in;
    private PrintWriter out;
    ServerSocket echoServerSocket;
    Socket clientSocket;
    int listenPort;
   public static int i=0;
    
    public Server(int port)
    {
        in = null;
        out = null;
        echoServerSocket = null;
        clientSocket = null;
        listenPort = port;
    }
    
    // Starts listening
    public void initialize() throws IOException
    {
        // Listen
        echoServerSocket = new ServerSocket(listenPort);
        i++;
        System.out.println("Listening...");
        i++;
        // Accept connection
        clientSocket = echoServerSocket.accept();
        i++;
        
        
        System.out.println("Accepted connection from client!");
        System.out.println("The client is from: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
        i++;
        // Initialize I/O
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
        i++;
        System.out.println("I/O initialized");
    }
    
    public void process() throws IOException
    {
        String msg;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while ((msg = in.readLine()) != null)
        {
            System.out.println("Received \"" + msg + "\" from client");
            
            if (msg.equals("bye"))
            {
                //out.println("Ok, bye bye...");
                out.print("bye");
                break;
            }
            else
            {
            	//out.print(br.readLine() + "\n");
            	//out.print(System.in.read() + "\n");
                //out.print(msg + "\n");
                out.flush();
            }
        }
    }
    
    // Closes the connection
    public void close() throws IOException
    {
        in.close();
        out.close();
        clientSocket.close();
        echoServerSocket.close();
    }
    
    public static void main(String[] args) throws IOException
    {
        // Get port
        int port = 2500;
        
        Server echoServer = new Server(port);
        
        // Listen on port
        try {
            echoServer.initialize();
        } catch (IOException e) {
            System.out.println("Failed to initialize on port " + port+"    at i: "+ i);
            System.exit(1);
        }
        
        // Process messages from client
        try {
            echoServer.process();
        } catch (IOException e) {
            System.out.println("Exception in processing");
            echoServer.close();
            System.exit(1);
        }
        
        System.out.println("Client disconnected - bye bye...");
        
        echoServer.close();
    }
}*/