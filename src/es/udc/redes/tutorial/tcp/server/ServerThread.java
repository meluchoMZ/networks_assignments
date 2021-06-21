
package es.udc.redes.tutorial.tcp.server;

import java.io.*;
import java.net.*;

public class ServerThread extends Thread {
    private Socket socketCliente;
    
    public ServerThread(Socket sc) {
        super();
        this.socketCliente = sc;
    }
    
    @Override
    public void run() {
                
        try {
                // Set the input channel
                InputStream input = new BufferedInputStream(socketCliente.getInputStream());
                // Set the output channel
                OutputStream output = new BufferedOutputStream(socketCliente.getOutputStream());
                
                byte datos[] = new byte[1024];
                int bytesLidos;
                // Receive the client message
                bytesLidos = input.read(datos);
                // Send response to the client
                if (bytesLidos > 0) {
                    output.write(datos);
                    output.flush();
                }
                // Close the streams
                input.close();
                output.close();
        } catch (SocketTimeoutException e){
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
