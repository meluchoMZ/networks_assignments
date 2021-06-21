package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;
import javax.net.ServerSocketFactory;

/**
 * Multithread TCP echo server.
 */
public class TcpServer {

    public static void main(String argv[]) throws IOException {
        if (argv.length != 1) {
            System.err.println("Format: TcpServer <port>");
            System.exit(-1);
        }
        int numeroPorto = Integer.parseInt(argv[0]);
        ServerSocket socketTcp = null;
        ServerSocketFactory serverFactory;
        try {
            // Create a server socket
            // Creando con factoria adaptase o firewall local
            
            serverFactory = ServerSocketFactory.getDefault();
            
            socketTcp = serverFactory.createServerSocket(numeroPorto);
            // Set a timeout of 300 secs
            socketTcp.setSoTimeout(300000);
            
            Socket socketCliente;
            while (true) {
                // Wait for connections
                socketCliente = socketTcp.accept();
                
                ServerThread st = new ServerThread(socketCliente);
                
                st.start();
                
            }
        // Uncomment next catch clause after implementing the logic            
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            //Close the socket
            if (socketTcp != null) socketTcp.close();
        }
    }
}
