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
import java.util.Random;

import org.apache.log4j.Logger;

import server.Transaction;

class ChainClient extends Thread {

	final static Logger logger = Logger.getLogger(ChainClient.class);

	String[] arguments;

	public ChainClient(String args) {
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

		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = null;
		byte[] receiveData = new byte[1024];
		BufferedReader br = new BufferedReader(new FileReader(new File(
				"ClientRandom.txt"))); // ../config/
		String sCurrentLine;
		String[] arguments = null;
		while ((sCurrentLine = br.readLine()) != null) {
			arguments = sCurrentLine.split(",");
		}
		Random random = new Random();
		int calls = 0;
		while (calls<1500) {
			Transaction trax = null;
			calls++;
			if (calls <= Integer.parseInt(arguments[0])) {
				int nextRandom = random.nextInt(Integer.parseInt(arguments[0]));
				boolean tailReq = false;
				if (nextRandom >= 0
						&& nextRandom < (Integer.parseInt(arguments[0]) * Double
								.parseDouble(arguments[1]))) {

					trax = new Transaction(calls,
							((int) (Math.random() * 2)) + 1,
							(((int) (Math.random() * 5)) + 1) * 100, "Deposit");

				} else if (nextRandom < Integer.parseInt(arguments[0])
						* (Double.parseDouble(arguments[1]) + Double
								.parseDouble(arguments[2]))) {

					trax = new Transaction(calls,
							((int) (Math.random() * 2)) + 1,
							(((int) (Math.random() * 5)) + 1) * -100,
							"Withdraw");

				} else {
					tailReq = true;
					trax = new Transaction(calls,
							((int) (Math.random() * 2)) + 1, "Enquiry");
				}
				if(calls == 100){
					trax.setDestaccountNo(2);
					trax.setType("Transfer");
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

					clientSocket.setSoTimeout(5000);
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