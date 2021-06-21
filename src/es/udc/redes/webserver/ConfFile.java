package es.udc.redes.webserver;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Date;

/**
 * This class contains methods to server Configuration Files management. 
 * @author Miguel Blanco Godón.
 */
public final class ConfFile {
    private int port;
    private String directoryIndex;
    private String localServerDirectory;
    private boolean allowClause;
    
    /**
     *  Creates a ConfFile object, trying to read a file called server_properties.txt. If it doesn't exists it is initialized to default values.
     */
    public ConfFile() {
        this.port = 5000;
        this.directoryIndex = "index.html";
        this.localServerDirectory = System.getProperty("user.dir");
        this.allowClause = false;
        setParameters();
    }
    
    /**
     * Gives the port where the server should open it's connections.
     * @return the port written of the server_properties.txt file or 5000 (default value). 
     */
    public int getPort() { 
        return this.port;
    }
    
    /**
     * Gives the default file to use when a directory is requested.
     * @return a String containing the name of that default file.
     */
    public String getDirIndex() {
        return this.directoryIndex;
    }
    
    /**
     * Gives the directory where the server files are allocated.
     * @return a String containing the name of that directory.
     */
    public String getLocalServerDir() {
        return this.localServerDirectory;
    }
    
    /**
     * Gives the value of the allow clause, meaning the server forwarding.
     * @return a boolean, true if the file contains "ALLOW: true", false on other case.
     */
    public boolean getAllowClause() {
        return this.allowClause;
    }
    
    /**
     * This method changes the values of the class. It's called by the constructor.
     */
    private void setParameters() {
        List<String> parameters = readConfigFile();
        if (parameters == null) return;
        
        this.port = (int) Integer.decode(parameters.get(0));
        this.directoryIndex = parameters.get(1);
        this.localServerDirectory = parameters.get(2);
        switch(parameters.get(3)) {
            case "true" : this.allowClause = true;break;
            default : this.allowClause = false;break;
        }
    }
    
    /**
     * Reads the file "server_properties.txt". 
     * @return a List<String> containing the information retrieved or null on error.
     */
    private List<String> readConfigFile() {
        File configFile = new File(System.getProperty("user.dir") + File.separator + "server_properties.txt");
        
        List<String> lines = null;
        List<String> options;
        
        try {
            lines = Files.readAllLines(configFile.toPath());
        } catch (IOException ioex) {
            System.out.println("Error: Cannot read server Config File. Make sure you have a 'server_properties.txt' file in your working directory");
            System.err.println("Error: " + ioex.getMessage());
        }
        
        if (lines == null) return null;
        
        options = new ArrayList();
        
        try {
            for (int i = 0; i < 4; i++) options.add(lines.get(i).split(" ")[1]);
        } catch (Exception e) {
            System.out.println("Error: Cannot create List parameter. Revise server_properties.txt content. Shoud be: ");
            System.out.println("PORT: <port_number> \nDIRECTORY_INDEX: <default_file_name>\nDIRECTORY: <directory_of_server_resources>\nALLOW: <true/false>");
            System.err.println("Error: " + e.getMessage());
            return null;
        }
        return options;
    }
    
    /**
     * Creates a dynamic html by reading the specified directory. It appends a 
     * link to the resource.
     * @param path a String containing the complete path to the directory.
     * @return a String containing the dynamic html.
     */
    static public String buildHTMLindexFile(String path) {
        File dir = new File(path);
        StringBuilder file = new StringBuilder();
        String[] strArr;
            file.append("<!DOCTYPE html>\n<html>\n<body>\n\n<h1>Requested directory files</h1>\n\n<p>");
            // for loop to read directory
            for (File fileOfDir : dir.listFiles()){ 
                strArr = fileOfDir.toString().split("/");
                file.append("<a href=");
                file.append(strArr[strArr.length - 1]);
                file.append("> ");
                file.append(fileOfDir.getName());
                file.append("</a>\n");
            }
            file.append("</p>\n\n</body>\n</html>\n");
        return file.toString();
    }
    
    /**
     * Creates a dynamic HTTP answer. It always returns 200 HTTP code.
     * @param resourceSize a long with the size of the answer.
     * @return a String containing the header of the HTTP answer.
     */
    public static String buildDynamicAnswer(long resourceSize) {
        StringBuilder answer = new StringBuilder();
        answer.append("HTTP/1.0 200 OK\nDate: ");
        answer.append(String.format("%tc", new Date()));
        answer.append("\nServer: ");
        answer.append(System.getProperty("os.name"));
        answer.append("\nContent-Length: ");
        answer.append(resourceSize);
        answer.append("\nContent-Type: text/html\n\n");
        return answer.toString();
    }
}
