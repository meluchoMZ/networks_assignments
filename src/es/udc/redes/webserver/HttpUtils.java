package es.udc.redes.webserver;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

/**
 * This class provides functionality for an HTTP/1.0 server.
 * @author Miguel Blanco Godón.
 */
public final class HttpUtils {
    
    
    // The constructor is declared private to prevent instantiation. 
    private HttpUtils() {
        
    }
    
    
    /**
     * This method implements searches for the If-Modified-Since header on 
     * the HTTP petition.
     * @param lines a String array where all HTTP client's petition lines are stored
     * @return a String with the date of the If-Modified-Since line on success; null on error. 
     */
    public static String ifModifiedSince(String[] lines) {        
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].contains("If-Modified-Since")) {
                StringBuilder sb = new StringBuilder(lines[i]);
                sb.delete(0, 19);
                return sb.toString();
            }
        }
        return null;
    }
    
        /**
     * This method updates log files for server management. If the logs files doesn't exist, it creates them.
     * @param petition a String array with the HTTP 1st header line(1 word/position).
     * @param validRequest a boolean, true if a correct petition was received(200,304 HTTP codes), false with other conditions.
     * @param answer a String with the HTTP answer sent to the HTTP client.
     * @param size a long with the size of the file in bytes.
     * @param serverThread a serverThread from the caller thread.
     */
    public static void updateLog(String[] petition, boolean validRequest, String answer, long size, WebServerThread serverThread) {
        int i = 0;
        String accessLogDir = serverThread.getConfigurationFile().getLocalServerDir() + File.separator + "access_log.txt";
        String errorsLogDir = serverThread.getConfigurationFile().getLocalServerDir() + File.separator + "errors_log.txt";
        String request;
        if (validRequest) request = accessLogDir; else request = errorsLogDir;
        try {
            Files.write(Paths.get(request), ("Petition received:").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            while (i < petition.length && i < 3) {
                Files.write(Paths.get(request), (" "+petition[i]).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                i++;
            }
            Files.write(Paths.get(request), (System.lineSeparator()+"From: "+serverThread.getCustomerSocket().getRemoteSocketAddress().toString()+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            Files.write(Paths.get(request), (String.format("Date: %tc", serverThread.getReceptionTime())+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            Files.write(Paths.get(request), ("Server answer: "+answer+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            if (validRequest) Files.write(Paths.get(request), ("Sent resource size: "+Long.toString(size)+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            Files.write(Paths.get(request), System.lineSeparator().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println("ERROR: CANNOT WRITE LOG FILE");
            System.err.println("Error: " + e.getMessage());
        }
        
    }
    
    /**
     * This method creates a byte[] that contains the answer to the HTTP client.
     * @param header the String that contains the "literal" part of the HTTP protocol.
     * @param resource a byte[] where the resource desired is stored, or null.
     * @return a byte [] that contains both the literal part and the resource, or only the literal part (on HEAD petition or 304 code), ready to be sent.
     */
    public static byte[] convertToByte(String header, byte[] resource) {
        int i = 0, j;
        byte [] answer;
        // Copyes both the header and the resource in the output byte array
        byte [] stringBytes = header.getBytes();
        if (resource != null) {
             answer = new byte[stringBytes.length + resource.length];
            for (i = 0; i < stringBytes.length; i++) answer[i] = stringBytes[i];
            j = i;
            for (i = j; i < answer.length; i++) answer[i] = resource[i-j];
        } else { answer = stringBytes;}
        
        return answer;
        
    }
    /**
     * Reads a file from a path.
     * @param path a String that contains the path to the file.
     * @return a byte[] with the file stored on it.
     */
    public static byte[] readFile(String path) {
        File file = new File(path);
        if (!file.exists() || !file.canRead()) return null;
        try {
            byte[] byteFile = Files.readAllBytes(Paths.get(path));
            return byteFile;
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Implements HTTP GET functionality.
     * @param petition a String array with the first line of the petition.
     * @param lines a String array with all the petition.
     * @param serverThread a serverThread from the caller thread.
     * @return a byte[] with the answer, ready to be sent. 
     */
    public static byte[] getMethod(String[] petition, String[] lines, WebServerThread serverThread) {
        String resource = petition[1];
        
        // Gets the resource path from the working directory
        StringBuilder resourcePath = new StringBuilder(serverThread.getConfigurationFile().getLocalServerDir());
        resourcePath.append(resource);
        
        // Checks if the resource is a directory. If it is, it tryes the default file.
        if ((new File(resourcePath.toString()).isDirectory()) && serverThread.getConfigurationFile().getAllowClause()) {
            File tryFile = new File(resourcePath + serverThread.getConfigurationFile().getDirIndex());
            if (tryFile.exists() && serverThread.getConfigurationFile().getAllowClause()) {resourcePath.append(serverThread.getConfigurationFile().getDirIndex());}
            else {
                // Returns html directory index
                String res = ConfFile.buildHTMLindexFile(serverThread.getConfigurationFile().getLocalServerDir()+petition[1]);
                return convertToByte(ConfFile.buildDynamicAnswer(res.length()), res.getBytes()); 
            }
        }
        // Checks if the If-Modified-Since is in the petition and if it matches with file's last modification
        File file = new File(resourcePath.toString());
        
        String ifModSince = ifModifiedSince(lines);
        if (ifModSince!=null) {
            if (ifModSince.equals(String.format("%tc", file.lastModified())))
                return convertToByte(getHeader(petition,false, true, serverThread),null);
        }
        // Reads the file
        
        byte[] resourceFile = readFile(resourcePath.toString());
        
        if(resource.contains(".do")) {
            String str = (DynamicPages.getDynamicResource(DynamicPages.parseVariables(resource)));
            byte[] din;
            if (str != null) din = str.getBytes();
            else din = null;
            return convertToByte(DynamicPages.getDynamicHeader((long) din.length),din);
        }
        return convertToByte(getHeader(petition,false, false, serverThread), resourceFile);
    }

    /**
     * Creates the "literal" part of a HTTP answer (HTTP/1.0 200 OK Date: Sat... Server: Apache/2.4.7...)
     * @param petition a String array with the HTTP petition line.
     * @param error a boolean value, true if the petition is incorrect.
     * @param modSince a boolean value, true if the resource hasn't been modified since last access to it.
     * @param serverThread a serverThread from the caller server.
     * @return a String with the HTTP answer.
     */
    public static String getHeader(String [] petition, boolean error, boolean modSince, WebServerThread serverThread) {
        // Creates a String to make the header of the answer
        StringBuilder header = new StringBuilder("HTTP/1.0 ");
        String answer;
        long size = 0;
        // Is used as a flag for the updatelog method
        boolean valid=false;
        StringBuilder resourcePath = new StringBuilder(serverThread.getConfigurationFile().getLocalServerDir());
        if (petition.length >=2) {
            String resource = petition[1];
            resourcePath.append(resource);
        }
        // Tryes to access the file
        File resourceFile = new File(resourcePath.toString());
        // Depending on the parsing and the resource properties, it creates diferent headers
        // Creates the state line
        if (modSince) {header.append("304 Not Modified"); answer = "304 Not Modified"; valid=true;}
        else if (error) {header.append("400 Bad Request"); answer = "400 Bad Request";}
        else if (!resourceFile.exists()) {header.append("404 Not Found"); answer = "404 Not Found";}
        else if (!serverThread.getConfigurationFile().getAllowClause() && (resourceFile.isDirectory() || !resourceFile.canRead())) {header.append("403 Forbidden"); answer = "403 Forbidden";}
        else {header.append("200 OK"); answer = "200 OK"; valid=true;}
        header.append(System.lineSeparator());
        //Creates the header lines
        // Date line
        header.append(String.format("Date: %tc", new Date()));
        header.append(System.lineSeparator());
        // Server info line
        header.append("Server: ");
        header.append(System.getProperty("os.name"));
        header.append(System.lineSeparator());
       
        if (resourceFile!=null && !error && !modSince) {
            
            // Content length and content type(if is a file with permissions
            if (!resourceFile.isDirectory() && resourceFile.canRead()) {
                
                // Content length
                header.append("Content-Length: ");
                header.append(resourceFile.length());
                header.append(System.lineSeparator());
                size = resourceFile.length();
                try {
                // Content type
                header.append("Content-Type: ");
                header.append(Files.probeContentType(resourceFile.toPath()));
                header.append(System.lineSeparator());
                 // Last modified line
                header.append(String.format("Last-Modified: %tc", resourceFile.lastModified()));
                header.append(System.lineSeparator());
                } catch (IOException e) {
                    System.out.println("CANNOT GET FILE PATH");
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
        // Blank line (End of header)
        header.append(System.lineSeparator());
        // It updates log files
        updateLog(petition, valid, answer, size, serverThread);
        return header.toString();
    }
    
    /**
     * It separates the string and checks if it's syntax is ok and gives the HTTP answer.
     * @param str a String with the HTTP petition.
     * @param serverThread a serverThread from the caller thread.
     * @return a byte[] with the HTTP answer.
     */
    public static byte[] parse(String str, WebServerThread serverThread) {
        //System.out.println(str);
        // Separates petition in lines
        String[] lines = str.split("\\r?\\n|\\r");
        // Separates the string in tokens
        String[] tokens = lines[0].split(" ");
        //ifModifiedSince(lines,false);
        // if empty return 400 BAD REQUEST HTML ERROR
        if (tokens.length < 3) return getHeader(tokens, true, false, serverThread).getBytes();
        else {
            // Gives the answer depending on the request
            switch (tokens[0]) {
                case "GET" : return getMethod(tokens, lines, serverThread);
                case "HEAD" : return getHeader(tokens,false, false, serverThread).getBytes();
            }
        }
        return getHeader(tokens,true, false, serverThread).getBytes();
    }
    
}
