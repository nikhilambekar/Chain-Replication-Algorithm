package client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import server.Transaction;

class ChainClientR extends Thread {

	final static Logger logger = Logger.getLogger(ChainClientR.class);

	String[] arguments;

	public ChainClientR(String args) {
		arguments = args.split(" ");
	}

	public void run() {
		System.out.println("Client Start" + Thread.currentThread().getName());
		try {
			Thread.sleep(1000);
			doDBProcessing(this.arguments);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doDBProcessing(String args[]) throws InterruptedException,
			FileNotFoundException, SocketException, UnknownHostException,
			IOException, ClassNotFoundException {

		// public static void main(String args[]) throws Exception {

		DatagramSocket clientSocket = new DatagramSocket();

		int bankHeadPort = Integer.parseInt(args[0]);
		int bankTailPort = Integer.parseInt(args[1]);
		int idSuff = Integer.parseInt(Thread.currentThread().getName().replace("Client", ""))*100;
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = null;
		byte[] receiveData = new byte[1024];
		BufferedReader br = new BufferedReader(new FileReader(new File(
				"Clientreq"+(idSuff/100)+".txt")));
		String sCurrentLine;
		
		while (true) {
			Transaction trax = null;

			while ((sCurrentLine = br.readLine()) != null) {
				boolean tailReq = false;
				String[] arguments = sCurrentLine.split(",");
				if (arguments.length == 4) {
					int reqId = idSuff+Integer.parseInt(arguments[0]);
					int accountNumber = Integer.parseInt(arguments[1]);
					int amount = Integer.parseInt(arguments[2]);
					if (arguments[3].equals("Withdraw")) {
						amount = amount * -1;
					}
					trax = new Transaction(reqId, accountNumber, amount,
							arguments[3]);
				} else if (arguments.length == 3) {
					int reqId = idSuff+Integer.parseInt(arguments[0]);
					int accountNumber = Integer.parseInt(arguments[1]);
					trax = new Transaction(reqId, accountNumber, arguments[2]);
					tailReq = true;
				} else if (arguments.length == 5 && idSuff != 300) {
					int reqId = idSuff+Integer.parseInt(arguments[0]);
					int accountNumber = Integer.parseInt(arguments[1]);
					int amount = -1*Integer.parseInt(arguments[2]);
					
					trax = new Transaction(reqId, accountNumber, amount,
							arguments[3]);
					trax.setDestaccountNo(Integer.parseInt(arguments[4]));
				} else {
					logger.debug("Invalid request : " + sCurrentLine);
				}
				boolean received = false;
				while (!received) {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(trax);
					sendData = outputStream.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, tailReq ? bankTailPort
									: bankHeadPort);
					clientSocket.send(sendPacket);
					System.out.println(this.getName() + " Requesting server "
							+ trax.toString());

					clientSocket.setSoTimeout(10000);
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					try {
						clientSocket.receive(receivePacket);
						receiveData = receivePacket.getData();
						ByteArrayInputStream bin = new ByteArrayInputStream(
								receiveData);
						ObjectInputStream is = new ObjectInputStream(bin);
						Transaction rectrax = (Transaction) is.readObject();
						System.out.println(this.getName() + " Received reply "
								+ rectrax.toString());
						received = true;
					} catch (Exception e) {
						received = false;
					}
				}
			}
		}
	}
}