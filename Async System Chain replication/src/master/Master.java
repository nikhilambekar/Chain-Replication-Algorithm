package master;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import server.FailureObj;

public class Master extends Thread {

	final static Logger logger = Logger.getLogger(Master.class);
	private Integer addNew;
	public static HashMap<Integer, Date> serverNodes = new HashMap<Integer, Date>();
	public static HashMap<Integer, Socket> serverNodesSocket = new HashMap<Integer, Socket>();
	public static HashMap<Integer, Integer> bankMapping = new HashMap<Integer, Integer>();
	public static ArrayList<Integer> serverNodesOrder = new ArrayList<Integer>();
	public static ArrayList<Integer> clientNodes = new ArrayList<Integer>();
	public static ReentrantLock lock = new ReentrantLock();
	public static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	public static ObjectOutputStream os = null;
	public static String STAT_NORMAL = "nor";
	public static String STAT_NEW = "new";
	public static ArrayList<Integer> head = new ArrayList<Integer>();
	public static ArrayList<Bank> bankList = new ArrayList<Bank>();
	public static ArrayList<Integer> tail = new ArrayList<Integer>();

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				"BankInfo.txt")));
		String sCurrentLine;
		int i = 0;
		while((sCurrentLine = br.readLine()) != null){
			String nextLine = br.readLine();
			String lastLine = br.readLine();
			i++;
			Bank bnk = new Bank();
			bnk.setBankNumber(i);
			String[] elements = sCurrentLine.split(",");
			for(String element : elements){
				bnk.getMembers().add(Integer.parseInt(element));
				bankMapping.put(Integer.parseInt(element), i);
			}
			head.add(Integer.parseInt(elements[2]));
			tail.add(Integer.parseInt(elements[0]));
			elements = nextLine.split(",");
			bnk.setHeadDatagramPort(Integer.parseInt(elements[0]));
			bnk.setTailDatagramPort(Integer.parseInt(elements[1]));
			elements = lastLine.split(",");
			bnk.setTailTransferIn(Integer.parseInt(elements[0]));
			bnk.setTailTransferOut(Integer.parseInt(elements[1]));
			bankList.add(bnk);
		}
		
		DatagramSocket serverSocket = null;

		byte[] receiveData = new byte[1024];
		try {
			serverSocket = new DatagramSocket(10008);
			logger.debug("Connection Socket Created");
			try {
				while (true) {
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					byte[] data = receivePacket.getData();
					String port = new String(data, 0, data.length);

					lock.lock();
					ArrayList<String> ports = new ArrayList<String>();
					ports.add((String) port.substring(0, 4));
					ports.add((String) port.substring(5, 9));
					String status = (String) port.substring(10, 13);
					if (serverNodes.isEmpty()) {
						new Master();
					}

					logger.debug("Received Ping server : " + ports + " "
							+ status);

					if (!serverNodes
							.containsKey(Integer.parseInt(ports.get(0)))) {
						Socket newSocket = new Socket(
								InetAddress.getLocalHost(),
								Integer.parseInt(ports.get(1)));
						serverNodesSocket.put(Integer.parseInt(ports.get(0)),
								newSocket);
						serverNodes.put(Integer.parseInt(ports.get(0)),
								new Date());
						if (status.equals(STAT_NEW)) {
							serverNodesOrder.add(0,
									Integer.parseInt(ports.get(0)));
							new Master(new Integer(Integer.parseInt(ports
									.get(0))));
						} else {
							serverNodesOrder
									.add(Integer.parseInt(ports.get(0)));
						}
					} else {
						serverNodes.put(Integer.parseInt(ports.get(0)),
								new Date());
					}
					lock.unlock();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Accept failed.");
				System.exit(1);
			} finally {
				lock.unlock();
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port: 10008.");
			System.exit(1);
		} finally {
			serverSocket.close();
		}
	}

	private Master() {
		start();
	}

	public Master(int addNew) {
		this.addNew = addNew;
		start();
	}

	public void run() {
		boolean runContinous = true;
		while (runContinous) {
			if (addNew == null) {
				Date testDate = new Date();
				try {
					// lock.lock();
					Integer failed = null;
					for (Map.Entry<Integer, Date> entry : serverNodes
							.entrySet()) {

						if (((testDate.getTime() - (entry.getValue()).getTime()) / 1000) >= 10) {
							logger.debug("Failed : " + entry.getKey());
							invokeRecovery(entry.getKey());
							failed = entry.getKey();
						}
					}
					if (failed != null) {
						serverNodes.remove(failed);
					}
				} catch (Exception e) {

				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				addNewServer(addNew);
				runContinous = false;
			}
		}
	}

	private void invokeRecovery(Integer key) {

		try {
			os = new ObjectOutputStream(outputStream);

			FailureObj obj = new FailureObj();
			int destPort = 0;
			if (serverNodesOrder.indexOf(key) == (serverNodesOrder.size() - 1)) {
				serverNodesOrder.remove(key);
				obj.setHead(true);
				obj.setDatagramPort(bankList.get(bankMapping.get(key)-1).getHeadDatagramPort());
				destPort = serverNodesOrder.get(serverNodesOrder.size() - 1);
				head.remove(key);
				head.add(serverNodesOrder.get(serverNodesOrder.size() - 1));
			} else if (serverNodesOrder.indexOf(key) == 0) {
				serverNodesOrder.remove(key);
				tail.remove(key);
				tail.add(serverNodesOrder.get(0));
				obj.setTail(true);
				obj.setDatagramPort(bankList.get(bankMapping.get(key)-1).getTailDatagramPort());
				destPort = serverNodesOrder.get(0);

			} else {
				int serverIndex = serverNodesOrder.indexOf(key);
				Integer pre = serverNodesOrder.get(serverIndex - 1);
				Integer post = serverNodesOrder.get(serverIndex + 1);
				logger.debug("Pre : " + pre);
				logger.debug("Post : " + post);
				boolean wasHead = head.contains(key);
				boolean wasTail = tail.contains(key);
				if (wasHead || wasTail) {
					if (wasHead) {
						FailureObj obj2 = new FailureObj();
						obj2.setDatagramPort(bankList.get(bankMapping.get(key)-1).getHeadDatagramPort());
						obj2.setPrevPort(post);
						obj2.setHead(true);
						head.remove(key);
						head.add(pre);
						os = new ObjectOutputStream(serverNodesSocket.get(pre)
								.getOutputStream());
						os.writeObject(obj2);
						FailureObj obj3 = new FailureObj();
						obj3.setNextPort(pre);
						obj3.setNextBankPort(getNextTail(post));
						obj3.setTail(true);
						obj3.setDatagramPort(bankList.get(bankMapping.get(post)-1).getTailDatagramPort());
						obj3.setTransferInPort(bankList.get(bankMapping.get(post)-1).getTailTransferIn());
						obj3.setTransferOutPort(bankList.get(bankMapping.get(post)-1).getTailTransferOut());
						os = new ObjectOutputStream(serverNodesSocket.get(post)
								.getOutputStream());
						logger.debug(obj3);
						os.writeObject(obj3);
					} else {
						FailureObj obj2 = new FailureObj();
						obj2.setPrevPort(post);
						obj2.setHead(true);
						tail.remove(key);
						tail.add(post);
						os = new ObjectOutputStream(serverNodesSocket.get(pre)
								.getOutputStream());
						os.writeObject(obj2);
						os.flush();

						FailureObj obj3 = new FailureObj();
						obj3.setPrevBankPort(getNextTail(post));
						obj3.setTail(true);
						os = new ObjectOutputStream(serverNodesSocket.get(
								getNextTail(key)).getOutputStream());
						os.writeObject(obj3);
						os.flush();
						
						FailureObj obj4 = new FailureObj();
						obj4.setNextPort(pre);
						obj4.setNextBankPort(getNextTail(post));
						obj4.setTail(true);
						obj4.setDatagramPort(bankList.get(bankMapping.get(post)-1).getTailDatagramPort());
						obj4.setTransferInPort(bankList.get(bankMapping.get(post)-1).getTailTransferIn());
						obj4.setTransferOutPort(bankList.get(bankMapping.get(post)-1).getTailTransferOut());
						os = new ObjectOutputStream(serverNodesSocket.get(post)
								.getOutputStream());
						os.writeObject(obj4);
						os.flush();
						logger.error("Tail failed" + post);
					}
					serverNodesOrder.remove(key);
					return;

				}
				FailureObj obj2 = new FailureObj();
				obj2.setPrevPort(post);
				os = new ObjectOutputStream(serverNodesSocket.get(pre)
						.getOutputStream());
				os.writeObject(obj2);
				destPort = post;
				obj.setNextPort(pre);

			}
			logger.error("Dest Port:" + destPort);
			os = new ObjectOutputStream(serverNodesSocket.get(destPort)
					.getOutputStream());
			os.writeObject(obj);

		} catch (Exception e) {
			//e.printStackTrace();
			serverNodesOrder.remove(key);
			logger.error("Error:" + e.toString());
		}
	}

	private int getNextTail(int post) {
		int nextTail = 0;
		for (int i = serverNodesOrder.indexOf(post) - 1; i >= 0; i--) {
			int port = serverNodesOrder.get(i);
			if (tail.contains(port)) {
				nextTail = port;
				break;
			}
		}
		return nextTail;
	}

	private void addNewServer(Integer newServer) {

		try {
			os = new ObjectOutputStream(outputStream);

			int order = serverNodesOrder.indexOf(newServer);
			int currentTail = serverNodesOrder.get(order + 1);
			FailureObj obj = new FailureObj();
			obj.setNextPort(newServer);
			obj.setTail(false);

			logger.debug("Changing tail");
			os = new ObjectOutputStream(serverNodesSocket.get(currentTail)
					.getOutputStream());
			os.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}