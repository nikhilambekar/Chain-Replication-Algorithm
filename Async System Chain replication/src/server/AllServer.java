package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

public class AllServer extends Thread {
	public static String hostname = "localhost";
	final static Logger logger = Logger.getLogger(AllServer.class);
	public static boolean head, tail, configState;
	public static String STAT_NORMAL = "nor";
	public static String STAT_NEW = "new";
	public static String RECEIVE = "receive";
	public static String SEND = "send";
	public static String UNBOUND = "unbound";
	public static String endtype;
	public static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	public static DatagramSocket serverSocket = null;
	public static ObjectInputStream inStream = null;
	public static ObjectOutputStream os = null;
	public static Socket socketN = null;
	public static ServerSocket socketTransferOut = null;
	public static Socket prevTransferOut = null;
	public static ServerSocket socketB = null;
	public static Socket socketTransferIn = null;
	public static ServerSocket socketMaster = null;
	public static Socket prevServer = null;
	public static byte[] sendData = new byte[1024];
	public static byte[] receiveData = new byte[1024];
	public static Map<Integer, Transaction> history = new HashMap<Integer, Transaction>();
	public static Map<Integer, Transaction> listHistory = new HashMap<Integer, Transaction>();
	public static Map<Integer, Transaction> sent = new HashMap<Integer, Transaction>();
	public static List<Transaction> transferList = new ArrayList<Transaction>();
	public static List<Transaction> transferListSent = new ArrayList<Transaction>();

	public static int rcvdMsgsCnt, fwdMsgsCnt, msgCount;
	public static int serverNext, serverBefore, serverMaster,
			serverTransferOut, serverTransferIn;
	public static String serverStatus = STAT_NORMAL;
	private boolean handleMaster, handleSent, handleTransfer, acceptConections;
	public static long TIME_OUT = 5000;

	public static ReentrantReadWriteLock serverLock = new ReentrantReadWriteLock();
	public static Lock readLock = serverLock.readLock();
	public static Lock writeLock = serverLock.writeLock();
	public static boolean fail;

	public static void main(String[] args) throws IOException {
		head = Boolean.parseBoolean(args[0]);
		tail = Boolean.parseBoolean(args[1]);
		serverNext = Integer.parseInt(args[2]);
		serverBefore = Integer.parseInt(args[3]);
		serverMaster = Integer.parseInt(args[4]);
		serverStatus = args[5];
		fail = Boolean.parseBoolean(args[6]);
		/*
		 * Initialization of all sockets
		 */
		if (head) {
			serverSocket = new DatagramSocket(Integer.parseInt(args[7]));
		}
		if (tail) {
			if (!head) {
				serverTransferOut = Integer.parseInt(args[8]);
				serverTransferIn = Integer.parseInt(args[9]);

				serverSocket = new DatagramSocket(Integer.parseInt(args[7]));
				serverSocket.setSoTimeout(5);
				new AllServer(false, false, true, false);
				new AllServer(false, false, true, true);
			}
			
		}
		if (serverStatus.equals(STAT_NEW)) {
			configState = true;
		}

		/*
		 * if (!tail) { try {
		 */
		try {
			socketN = new Socket(hostname, serverNext);
		} catch (Exception e) {

		}
		new AllServer(false, true, false, false);
		/*
		 * } catch (Exception e) { logger.debug("Next Server not available" +
		 * e); } }
		 */
		socketB = new ServerSocket(serverBefore);
		socketMaster = new ServerSocket(serverMaster);
		new AllServer(true, false, false, false); // Thread to send heartbeat to
													// Master
		new AllServer(false, false, false, false); // Thread to listen to Master

		while (true) {
			Transaction trax = null;
			if (!head || (head && args.length > 8))
				prevServer = socketB.accept();

			if (!head) {
				// prevServer = socketB.accept();
				if (serverStatus.equals(STAT_NEW)) {
					serverSocket = new DatagramSocket(Integer.parseInt(args[7]));
					serverSocket.setSoTimeout(5);
					// To receive History and Sent from Previous Tail
					inStream = new ObjectInputStream(
							prevServer.getInputStream());
					synchronized (inStream) {

						try {
							Map<Integer, Transaction> historyPrev = (Map<Integer, Transaction>) inStream
									.readObject();
							Map<Integer, Transaction> listhistoryPrev = (Map<Integer, Transaction>) inStream
									.readObject();
							Map<Integer, Transaction> sendPrev = (Map<Integer, Transaction>) inStream
									.readObject();
							history.putAll(historyPrev);
							listHistory.putAll(listhistoryPrev);
							sent.putAll(sendPrev);
							logger.error("History | Send Received");
							logger.debug(historyPrev + "\n" + listhistoryPrev
									+ "\n" + sendPrev);

						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}

					}
					configState = false;
					tail = true;
					serverStatus = STAT_NORMAL;
				}
			}

			while (true) {

				// Tail Case
				if (tail) {
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					try {
						serverSocket.receive(receivePacket);
						//checkforExit(1, 0);
					} catch (Exception e) {
						if (!head) {
							try {
								prevServer.setSoTimeout(5);
								inStream = new ObjectInputStream(
										prevServer.getInputStream());
								Object outputObj = (Transaction) inStream
										.readObject();
								if (outputObj instanceof Transaction) {
									//checkforExit(1, 0);
									trax = (Transaction) outputObj;
									history.put(trax.getAccountNo(), trax);
									listHistory.put(trax.getId(), trax);
									writeLock.lock();
									if (trax.getType().equals("Transfer")) {
										if (trax.getStatus() == null) {
											logger.debug("Transfer request to Head");
											os = new ObjectOutputStream(
													socketN.getOutputStream());
											os.writeObject(trax);
											os.flush();
											if (fail) {
												System.exit(0);
											}
											//checkforExit(0, 1);
											transferListSent.add(trax);
											// forwaded = true;
											sent.put(trax.getId(), trax);
										} else {
											transferList.add(trax);
										}
										continue;
									}
									writeLock.unlock();
									sendDataGramPacket(trax);
									//checkforExit(0, 1);
								} else if (outputObj instanceof HashMap) {
									Map<Integer, Transaction> sendPrev = (HashMap<Integer, Transaction>) outputObj;
									sent.putAll(sendPrev);
								}

							} catch (Exception ex) {
								// ex.printStackTrace();

								continue;
							}

						}
						continue;
					}

					trax = getTransactionObj(receivePacket);
					logger.debug("Tail Received Packet from Client : "
							+ receivePacket.getPort() + " " + trax.toString());
					updatePacketStatus(trax);
					sendDataGramPacket(trax);
					//checkforExit(0, 1);
					continue;
				}

				// Intermediate Servers
				if (!head) {
					try {
						if (!configState) {
							prevServer.setSoTimeout(0);
							inStream = new ObjectInputStream(
									prevServer.getInputStream());
							Object outputObj = (Transaction) inStream
									.readObject();
							if (outputObj instanceof Transaction) {
								//checkforExit(1, 0);
								trax = (Transaction) outputObj;
							} else if (outputObj instanceof HashMap) {
								Map<Integer, Transaction> sendPrev = (HashMap<Integer, Transaction>) outputObj;
								sent.putAll(sendPrev);
							}
						}
					} catch (Exception e) {
//						/e.printStackTrace();
					}
				} else {
					// Head
					serverSocket.setSoTimeout(5);
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					try {
						serverSocket.receive(receivePacket);
						trax = getTransactionObj(receivePacket);
					} catch (Exception e) {
						try {
							prevServer.setSoTimeout(5);
							inStream = new ObjectInputStream(
									prevServer.getInputStream());
							Object outputObj = (Transaction) inStream
									.readObject();
							if (fail) {
								System.exit(0);
							}
							if (outputObj instanceof Transaction) {
								logger.debug("transfer request from tail");
								trax = (Transaction) outputObj;
							} else if (outputObj instanceof HashMap) {
								Map<Integer, Transaction> sendPrev = (HashMap<Integer, Transaction>) outputObj;
								sent.putAll(sendPrev);
							}
						} catch (Exception e1) {
							// e1.printStackTrace();
							continue;
						}
					}

					//checkforExit(1, 0);

					logger.debug("Head Received Packet from Client : "
							+ receivePacket.getPort() + " " + trax);
					updatePacketStatus(trax);
				}
				if (trax != null) {
					if (!head) {
						history.put(trax.getAccountNo(), trax);
						listHistory.put(trax.getId(), trax);
					}
					if (!tail) {
						/*
						 * boolean forwaded = false; while (!forwaded) {
						 */try {
							if (!configState) {
								os = new ObjectOutputStream(
										socketN.getOutputStream());
								os.writeObject(trax);
								os.flush();
								//checkforExit(0, 1);
								// forwaded = true;
								sent.put(trax.getId(), trax);
								logger.debug("Forwarding message to next server : "
										+ socketN.getPort()
										+ " "
										+ trax.toString());
							}
						} catch (Exception e) {
							// forwaded = false;
							e.printStackTrace();
						}
						// }
					} else {
						sendDataGramPacket(trax);
						//checkforExit(0, 1);
					}
				}
				trax=null;

			}
		}

	}

	/*static void checkforExit(int received, int sent) {
		rcvdMsgsCnt += received;
		fwdMsgsCnt += sent;
		if (endtype.equals(RECEIVE)) {
			if (msgCount != 0 && rcvdMsgsCnt >= msgCount) {
				//System.exit(0);
			}
		}
		if (endtype.equals(SEND)) {
			if (msgCount != 0 && fwdMsgsCnt >= msgCount) {
				//System.exit(0);
			}
		}

	}*/

	static void sendDataGramPacket(Transaction trax) throws IOException {
		boolean transferred = false;

		try {
			outputStream.reset();
			os = new ObjectOutputStream(outputStream);
			os.writeObject(trax);
			sendData = outputStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, InetAddress.getLocalHost(),
					trax.getClientPort());
			serverSocket.send(sendPacket);
			logger.debug("Forwarding message to client : "
					+ trax.getClientPort() + " " + trax.toString());
			os.close();
			synchronized (prevServer) {
				os = new ObjectOutputStream(prevServer.getOutputStream());
				os.writeObject(trax);
			}

		} catch (Exception e) {
			sent.put(trax.getId(), trax);
			e.printStackTrace();
		}
	}

	static Transaction getTransactionObj(DatagramPacket receivePacket)
			throws IOException {
		Transaction trax = null;

		byte[] data = receivePacket.getData();
		ByteArrayInputStream bin = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(bin);
		int port = receivePacket.getPort();
		try {
			trax = (Transaction) is.readObject();
			trax.setClientPort(port);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return trax;
	}

	static Transaction updatePacketStatus(Transaction trax) throws IOException {
		if (!trax.getType().equalsIgnoreCase("Enquire")) {
			if (listHistory.containsKey(trax.getId())
					&& ((listHistory.get(trax.getId()).getAccountNo() != trax
							.getAccountNo()) || (!listHistory.get(trax.getId())
							.getType().equals(trax.getType())))) {
				trax.setType("Inconsistent with History");
			} else if (listHistory.containsKey(trax.getId())
					&& ((listHistory.get(trax.getId()).getAccountNo() == trax
							.getAccountNo()) && (listHistory.get(trax.getId())
							.getType().equals(trax.getType())))) {
				trax.setType("Already processed");
			} else {

				if (trax.getType().equals("Transfer")
						&& trax.getTransferbalance() > 0) {
					if (history.containsKey(trax.getDestaccountNo())) {
						trax.setBalance(history.get(trax.getDestaccountNo())
								.getBalance() + trax.getTransferbalance());
					} else {
						trax.setBalance(trax.getTransferbalance());
					}
					trax.setTransferbalance(0);
					trax.setStatus("Done");
					history.put(trax.getDestaccountNo(), trax);
					listHistory.put(trax.getId(), trax);
				} else if (history.containsKey(trax.getAccountNo())) {
					if ((history.get(trax.getAccountNo()).getBalance() + trax
							.getBalance()) >= 0) {
						if (trax.getType().equals("Transfer")) {
							if (trax.getTransferbalance() == 0) {
								trax.setTransferbalance(-1 * trax.getBalance());
								trax.setBalance(history
										.get(trax.getAccountNo()).getBalance()
										+ trax.getBalance());
								trax.setInitialBal(trax.getBalance());
							} else {
								trax.setBalance(history.get(
										trax.getDestaccountNo()).getBalance()
										+ trax.getTransferbalance());
								trax.setTransferbalance(0);
								trax.setStatus("Done");
							}
						} else {
							trax.setBalance(history.get(trax.getAccountNo())
									.getBalance() + trax.getBalance());
						}

						history.put(trax.getAccountNo(), trax);
						listHistory.put(trax.getId(), trax);

					} else {
						trax.setBalance(history.get(trax.getAccountNo())
								.getBalance());
						trax.setType("Invalid Request");
					}
				} else {
					if (trax.getBalance() >= 0
							&& !trax.getType().equals("Transfer")) {
						history.put(trax.getAccountNo(), trax);
						listHistory.put(trax.getId(), trax);
					} else if (trax.getTransferbalance() > 0
							&& trax.getType().equals("Transfer")) {
						trax.setBalance(trax.getTransferbalance());
						trax.setTransferbalance(0);
						trax.setStatus("Done");
						history.put(trax.getDestaccountNo(), trax);
						listHistory.put(trax.getId(), trax);

					} else {

						trax.setType("Invalid Request");

					}
				}
			}
		} else if (history.containsKey(trax.getAccountNo())) {
			trax.setBalance(history.get(trax.getAccountNo()).getBalance());
		} else {
			if ((trax.getBalance()) < 0) {
				trax.setType("Invalid Request");

			}
			logger.debug("Invalid request from client : "
					+ trax.getClientPort() + " " + trax.toString());
		}
		return trax;
	}

	static void handleFailure(FailureObj failedobj) {
		if (!head && failedobj.getHead() != null) {
			boolean headTemp = failedobj.getHead();
			if (headTemp) {
				try {
					logger.debug("Converting this to head " + serverBefore);
					serverSocket = new DatagramSocket(
							failedobj.getDatagramPort());
					head = headTemp;
					if (failedobj.getPrevPort() != 0) {
						try {
							logger.debug("Waiting for Before " + serverBefore
									+ (new Date()).getTime());
							prevServer.close();
							prevServer = socketB.accept();
							logger.debug("Connected to server before"
									+ (new Date()).getTime());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (!tail && failedobj.getTail() != null) {
			boolean tailTemp = failedobj.getTail();
			if (tailTemp) {
				try {
					logger.debug("Converting this to tail " + serverBefore);
					serverSocket = new DatagramSocket(
							failedobj.getDatagramPort());
					sent.size();
					serverSocket.setSoTimeout(5);
					tail = tailTemp;
					if (failedobj.getNextPort() != 0) {
						if (socketN != null) {
							socketN.close();
						}
						socketN = new Socket(hostname, failedobj.getNextPort());
						os = new ObjectOutputStream(socketN.getOutputStream());
						logger.debug("Sending Sent Transfer Values");
						for (Transaction value : sent.values()) {
							if (value.getType().equals("Transfer")) {

								os.writeObject(value);
								transferListSent.add(value);
								os.flush();
							}
						}

					}
					if (serverTransferIn == 0 || serverTransferOut == 0) {
						serverTransferIn = failedobj.getTransferInPort();
						serverTransferOut = failedobj.getTransferOutPort();
						new AllServer(false, false, true, false);
						new AllServer(false, false, true, true);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {

			if (failedobj.getNextPort() != 0) {
				try {

					logger.debug("Connecting to next " + serverBefore
							+ (new Date()).getTime());
					if (failedobj.getTail()) {
						if (socketN != null) {
							socketN.close();
						}
						socketN = new Socket(hostname, failedobj.getNextPort());
						os = new ObjectOutputStream(socketN.getOutputStream());
						for (int i = 0; i < transferListSent.size(); i++) {
							os.writeObject(transferListSent.get(i));
							os.flush();
							//checkforExit(0, 1);
						}
						os.close();

					} else {

						boolean send = false;
						if (tail) {

							configState = true;
							serverSocket.close();
							prevServer.setSoTimeout(0);
							tail = false;
							new AllServer(false, true, false, false);
							send = true;
						}
						if (socketN != null) {
							socketN.close();
						}
						socketN = new Socket(hostname, failedobj.getNextPort());
						os.reset();
						os = new ObjectOutputStream(socketN.getOutputStream());
						os.flush();
						if (send) {
							os.writeObject(history);
							os.flush();
							os.writeObject(listHistory);
							os.flush();
							logger.error("sent history");
							os.writeObject(sent);
							os.flush();
							logger.error("sent forward");
							configState = false;
						}
					}
					logger.debug("Connecting to next successfull "
							+ (new Date()).getTime());

				} catch (Exception e) {
					e.printStackTrace();

				}
			}
			if (failedobj.getPrevPort() != 0) {
				try {
					logger.debug("Waiting for Before " + serverBefore
							+ (new Date()).getTime());
					prevServer.close();
					prevServer = socketB.accept();
					logger.debug("Connected to server before"
							+ (new Date()).getTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (failedobj.getPrevBankPort() != 0) {
				try {
					logger.debug("Bank Link Failure" + +(new Date()).getTime());
					prevTransferOut.close();
					socketTransferOut.close();
					socketTransferOut = null;
					logger.debug("Bank Link Failure" + (new Date()).getTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

	}

	private AllServer(boolean handleMaster, boolean handleSent,
			boolean handleTransfer, boolean acceptConections) {
		this.handleMaster = handleMaster;
		this.handleSent = handleSent;
		this.handleTransfer = handleTransfer;
		this.acceptConections = acceptConections;

		start();
	}

	public void run() {

		Socket mastServer = null;
		while (true) {
			if (!handleSent && !handleTransfer) {
				if (!handleMaster) {
					outputStream.reset();
					try {
						String strObj = serverBefore + " " + serverMaster + " "
								+ serverStatus;
						sendData = strObj.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length,
								InetAddress.getLocalHost(), 10008);
						DatagramSocket clientSocket = new DatagramSocket();

						clientSocket.send(sendPacket);
						Thread.sleep(TIME_OUT);
					} catch (Exception e) {
						// e.printStackTrace();
					}
				} else {
					try {
						if (mastServer == null) {
							mastServer = socketMaster.accept();
						}
						inStream = new ObjectInputStream(
								mastServer.getInputStream());
						FailureObj obj = (FailureObj) inStream.readObject();
						logger.debug(" Received failure");
						handleFailure(obj);

					} catch (Exception e) {
						// e.printStackTrace();
					}

				}
			} else if (!handleTransfer) {
				try {

					if (!configState) {
						Transaction trax = null;
						// logger.error("sent before");
						ObjectInputStream inStreamThd = null;
						ObjectOutputStream osThd = null;
						if (!tail) {
							inStreamThd = new ObjectInputStream(
									socketN.getInputStream());
							trax = (Transaction) inStreamThd.readObject();
							sent.remove(trax.getId());

						}
						if (!head & (trax != null)) {
							osThd = new ObjectOutputStream(
									prevServer.getOutputStream());
							osThd.writeObject(trax);
						}
					}
				} catch (Exception e) {
					// e.printStackTrace();
				}
			} else {
				try {
					if (acceptConections) {
						if (socketTransferIn == null) {
							try {
								socketTransferIn = new Socket(hostname,
										serverTransferIn);
							} catch (IOException e) {
								continue;
							}

						}
						try {
							inStream = new ObjectInputStream(
									socketTransferIn.getInputStream());
							Object outputObj = (Transaction) inStream
									.readObject();
							transferListSent.remove(0);
							logger.error("Transfer Rcvd Obj");
							if (outputObj instanceof Transaction) {
								Transaction trax = (Transaction) outputObj;
								trax.setBalance(trax.getInitialBal());
								try {
									sendDataGramPacket(trax);
									sent.remove(trax.getId());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
						} catch (Exception e) {
							e.printStackTrace();
							socketTransferIn.close();
							socketTransferIn = null;

						}

					} else {
						try {
							if (socketTransferOut == null) {
								socketTransferOut = new ServerSocket(
										serverTransferOut);
								prevTransferOut = socketTransferOut.accept();
							}
							if (!transferList.isEmpty()) {
								logger.error("Transfer");
								os = new ObjectOutputStream(
										prevTransferOut.getOutputStream());
							}
							while (!transferList.isEmpty()) {
								os.writeObject(transferList.remove(0));
							}
							//os.close();
						} catch (Exception e) {
							prevTransferOut.close();
							socketTransferOut.close();
							socketTransferOut = null;

						}

					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}
	}
}
