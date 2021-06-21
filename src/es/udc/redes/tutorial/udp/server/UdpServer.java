package es.udc.redes.tutorial.udp.server;

import java.net.*;

/**
 * Implements an UDP Echo Server.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: UdpServer <port_number>");
            System.exit(-1);
        }
        // convirtese a Int o numero de porto introducido por terminal
        int porto = Integer.parseInt(argv[0]);
        
        DatagramSocket socketUDP = null;
        
        try {
            // Create a server socket
            socketUDP = new DatagramSocket(porto);
            // Set max. timeout to 300 secs
            socketUDP.setSoTimeout(300000);
            byte datos[] = new byte[1024];
            while (true) {
                // Prepare datagram for reception
                DatagramPacket paqueteRec = new DatagramPacket(datos, datos.length);
                
                // Receive the message
                socketUDP.receive(paqueteRec);
                // Prepare datagram to send response
                DatagramPacket paqueteEnv = new DatagramPacket(datos, datos.length, paqueteRec.getAddress(), paqueteRec.getPort());
                // Send response
                socketUDP.send(paqueteEnv);
            }
          
        // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socketUDP != null) socketUDP.close();
        }
    }
}
