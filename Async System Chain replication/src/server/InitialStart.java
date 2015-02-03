package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InitialStart {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException,InterruptedException {
		String sCurrentLine;

		BufferedReader br = new BufferedReader(new FileReader(new File(
				"ports.txt")));
		Process server = Runtime.getRuntime().exec("javac -cp log4j-1.2.17.jar; server.Transaction.java server.FailureObj.java server.AllServer.java");
		while ((sCurrentLine = br.readLine()) != null) {
			Process serverStart =
			Runtime.getRuntime().exec("java -cp log4j-1.2.17.jar; AllServer "+ sCurrentLine);
			System.out.println("Creating server " + sCurrentLine);
			Thread.sleep(1000);
			/*BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					serverStart.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					serverStart.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			String s = null;
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out
					.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}*/
		}
		
	}

}
