package eu.sendregning.oxalis;

public class Aoc {

	public static void main(String[] args) throws Exception {
    	System.setProperty("OXALIS_HOME", "C:\\workspaceWas\\peppol\\oxalis_home_prod");
    	String alex = "C:\\workspaceWas\\peppol\\standalone\\UBL21-FactIMI-alex.xml";
    	String billing = "C:\\workspaceWas\\peppol\\standalone\\UBL21-FactIMI-001.xml";
		String f = billing;
		//f = alex;
		String[] args2 = new String[]{
    			"-f", f,
    			"-r", "9920:ESP0801900B"
    	};
		Main.main(args2);

	}
}
