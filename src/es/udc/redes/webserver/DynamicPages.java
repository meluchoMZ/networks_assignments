package es.udc.redes.webserver;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * This class manages the entire server HTTP/1.0 GET response on dynamic pages.
 * Precondition - The class takes for granted that the petition received had this
 * format: "GET /filename.do?var0=value0&var1=value&...&varn=valuen HTTP/1.X". 
 * @author Miguel Blanco Godón
 */
public class DynamicPages {
    
    /**
     * Extracts the variables from the dynamic request.
     * @param petition a string containing the petition.
     * @return a pair "key,value" with each variable name and it's value, null 
     * if there aren't variables on the request.
     */
    static public Map<String, String> parseVariables(String petition) {
        Map<String, String> map = new HashMap();
        // Splits the petition in two parts, name and varibles
        String [] parts = petition.split("\\.do");
        if (parts.length!=2) return null;
        StringBuilder resourceName = new StringBuilder(parts[0]);
        
        // Removes / from the name and inserts on the HashMap
        resourceName.deleteCharAt(0);
        map.put("0x0Codename", "es.udc.redes.webserver.".concat(resourceName.toString()));
        
        // Extracts the variables from the petition and adds them into the HashMap
        StringBuilder variables = new StringBuilder(parts[1]);
        if (variables.charAt(0) == '/') variables.deleteCharAt(0);
        String [] vars = variables.toString().split("&");
        if (vars[0].charAt(0) == '?') vars[0]= vars[0].substring(1);
        for(String str : vars) {
            String [] pair = str.split("=");
            String key = pair[0];
            String value;
            if (pair.length == 1) value = "";
            else value = pair[1];
            map.put(key, value);
        }
        
        
        return map;
    }
    
    /**
     * Returns the answer to the dynamic request.
     * @param parameters a pair "key,value" containing each variable name and it's value
     * @return a string containing the answer to the HTTP client.
     */
    static public String getDynamicResource(Map<String, String> parameters) {
        //System.out.println("parametros" +parameters.get("name"));
        try {
            return ServerUtils.processDynRequest(parameters.get("0x0Codename"),parameters);
        } catch (Exception e) {
            System.out.println("Error: Cannot create dynamic resource");
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Returns the header of the dynamic resource.
     * @param strLen a long containing the length of the dynamic resource.1
     * @return a String containing the header.
     */
    static public String getDynamicHeader(long strLen) {
        StringBuilder header = new StringBuilder("");
        header.append("HTTP/1.0 200 OK\nDate: ");
        header.append(String.format("%tc", new Date()));
        header.append("\nServer: Ubuntu/19.10(Unix)\nContent-Length: ");
        header.append(Long.toString(strLen));
        header.append("\nContent-Type: text/html\n\n");
        return header.toString();
    }
}