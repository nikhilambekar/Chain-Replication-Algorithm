package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ClientThread {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		 BufferedReader br = new BufferedReader(new FileReader(new File("ClientPorts.txt"))); 
		 String input = null; 
		 int i=1;
		 while ((input =br.readLine()) != null) {
			 Thread t = new ChainClientR(input);
			 t.setName("Client" + (i));
			 t.start();
		 }
		/*Thread t = new ChainClientR("9876 9877");
		t.setName("Client" + (1));
		t.start();
		Thread t2 = new ChainClientR("9878 9879");
		t2.setName("Client" + (2));
		t2.start();
		Thread t3 = new ChainClientR("9880 9881");
		t3.setName("Client" + (3));
		t3.start();*/
	}
}
