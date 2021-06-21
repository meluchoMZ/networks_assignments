package es.udc.redes.tutorial.copy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Copy {

    public static void main(String[] args) throws IOException, RuntimeException {
        FileInputStream entrada = null;
        FileOutputStream saida = null;
        if (args.length != 2) {
            System.out.println("ERROR: entrada inválida.");
            System.out.println("O formato correcto é o seguinte:");
            System.out.println("   $ java es.udc.redes.tutorial.copy.Copy <fichero origen> <fichero destino>");
            return;
        }
        try {
            entrada = new FileInputStream(args[0]);
            saida = new FileOutputStream(args[1]);
            
            int _byte;
            while ((_byte = entrada.read()) != -1) {
                saida.write(_byte);
            }
        } catch (SecurityException s) {
            System.out.println("ERROR: Falta de privilexios para este ficheiro.");
        } catch (FileNotFoundException e) { 
            System.out.println("ERROR: Ficheiro non válido. Revise as rutas.");
        }finally {
            if (entrada != null) entrada.close();
            if (saida != null) saida.close();
        }
    }
    
}
