package es.udc.redes.webserver;
import java.io.*;
import java.net.*;
import java.util.Date;


/**
 * This class implements a thread that answers a HTTP/1.0 or HTTP/1.1 petition.
 * It implements GET and HEAD method. It is compatible with plain text, html, 
 * pdf, gif and png.
 * The HTTP codes implemented are 200, 304, 400, 403 and 404. 
 * Also, it creates and updates two log files: access_logs.txt and errors_logs.txt 
 * where statistics of the connection to the server are stored.
 * @author Miguel Blanco Godón.
 * 
 */
public class WebServerThread extends Thread {
    private final Socket customerSocket;
    private Date receptionTime;
    private final ConfFile configFile;
    
    /**
     * This is the WebServerThread constructor.
     * @param cs a socket from the caller, needed to manage communication.
     * @param cF a ConfFile, where some server info is.
     */
    public WebServerThread(Socket cs, ConfFile cF) {
        super();
        this.customerSocket = cs;
        this.configFile = cF;
    }
    
    /**
     * Get method to the socket.
     * @return a socket corresponding to the customer side of the connection.
     */
    public Socket getCustomerSocket() {
        return customerSocket;
    }
    
    /**
     * Gives the time when the message was received.
     * @return a Date in Date class format. 
     */
    public Date getReceptionTime() {
        return receptionTime;
    }
    
    /**
     * Gives the ConfFile containing some server info.  
     * @return a ConfFile with port, directory_index, working directory and allow clause info. 
     */
    public ConfFile getConfigurationFile() {
        return configFile;
    }
    
    /**
     * Receives a HTTP message, processes it, creates a HTTP/1.0 answer and sends it back.
     */
    @Override
    public void run() {
                
        try {
                // Sets the input channel
                InputStream input = new BufferedInputStream(customerSocket.getInputStream());
                // Sets the output channel
                OutputStream output = new BufferedOutputStream(customerSocket.getOutputStream());
                // Sets an array to get the data
                byte data[] = new byte[1024];
                
                int readBytes;
                // Receives the client message
                readBytes = input.read(data);
                
                
                // Saves the local time when the message was readed 
                this.receptionTime = new Date();
                
                
                // sets a byte array to save the answer
                byte answer[] = HttpUtils.parse(new String(data),this);
                
                // Sends response to the client
                if (readBytes > 0) {
                    output.write(answer);
                    output.flush();
                }
                // Closes the streams
                input.close();
                output.close();
        } catch (SocketTimeoutException e){
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                // Tryes to close the socket
                if (customerSocket != null) customerSocket.close();
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
