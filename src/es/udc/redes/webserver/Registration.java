package es.udc.redes.webserver;

import java.util.Map;

/**
 * This class implements the dynamic html for sing_up.html file
 * @author Miguel Blanco Godón
 */
public class Registration implements MiniServlet{
    public Registration(){
		
	}
	
        @Override
	public String doGet (Map<String, String> parameters){
		String userName = parameters.get("username");
		String name = parameters.get("name");
		String surname = parameters.get("surname");
                String mail = parameters.get("mail");
                
                String completeName = name + surname;
		
                return printHeader() + printBody(userName, completeName, mail) + printEnd();
	}	

	private String printHeader() {
		return "<html><head> <title>Confirmation</title> </head> ";
	}

	private String printBody(String user, String compName, String mail) {
		return "<body> <h1> Congratulations "+user+"! Registration completed succesfully</h1>"
                        + "<h2> Please check the following info:</h2>\n<p>Name: "
                        + compName + "</p>\n<p1> E-mail: " + mail
                        +"</p>\n <p> This data can be modified through the main page."
                        + "</p>\n <p>A verification e-mail will be sent soon. to "+mail+"</p>"
                        + "<p>Thank you for using this service</p></body>";
	}

	private String printEnd() {
		return "</html>";
	}
}
