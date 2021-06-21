package es.udc.redes.webserver;
import java.net.*;
import java.io.*;
import javax.net.ServerSocketFactory;

/**
 * 
 * This class creates a multi-thread HTTP/1.0 server. 
 * 
 * @author Miguel Blanco Godón
 * 
 */
public class WebServer {
    static private ConfFile configFile;
    /**
     * Starts a HTTP/1.0 web server and starts listening for incoming communications.
     * When a connection appears it creates a thread to manage the connection.
     * @param argv a String giving the parameters to start the program.
     * @throws IOException if no connections are established within 5 minutes(300 seconds).
     */
    public static void main(String[] argv) throws IOException {
        // Checks if the syntax is ok
        if (argv.length != 1) {
            System.err.println("Format: TcpServer <port>");
            System.exit(-1);
        }
        
        configFile = new ConfFile();
        
        ServerSocket tcpSocket = null;
        ServerSocketFactory serverFactory;
        WebServerThread serverThread;
        try {
            // Creates a server socket
            // It is created with ServerSocketFactory because using this class
            // the socket adequates to the local firewall
            
            serverFactory = ServerSocketFactory.getDefault();
            
            tcpSocket = serverFactory.createServerSocket(configFile.getPort());
            // Sets a timeout of 300 secs
            tcpSocket.setSoTimeout(300000);
            
            Socket customerSocket;
            while (true) {
                // Waits for connections
                customerSocket = tcpSocket.accept();
                
                serverThread = new WebServerThread(customerSocket, configFile);
                
                serverThread.start();
                
            }            
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Closes the socket if it is not null
            if (tcpSocket != null) tcpSocket.close();
        }
    }
    
}
