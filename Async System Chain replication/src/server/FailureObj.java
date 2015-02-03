package server;

import java.io.Serializable;

public class FailureObj implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private Boolean head; 
	private Boolean tail;
	private int nextPort;
	private int prevPort;
	private int prevBankPort;
	private int nextBankPort;
	private int transferOutPort;
	private int transferInPort;
	private int datagramPort;
	
	
	
	public Boolean getHead() {
		return head;
	}
	public void setHead(Boolean head) {
		this.head = head;
	}
	public Boolean getTail() {
		return tail;
	}
	public void setTail(Boolean tail) {
		this.tail = tail;
	}
	public int getNextPort() {
		return nextPort;
	}
	public void setNextPort(int nextPort) {
		this.nextPort = nextPort;
	}
	public int getPrevPort() {
		return prevPort;
	}
	public void setPrevPort(int prevPort) {
		this.prevPort = prevPort;
	}
	public int getDatagramPort() {
		return datagramPort;
	}
	public void setDatagramPort(int datagramPort) {
		this.datagramPort = datagramPort;
	}
	public int getPrevBankPort() {
		return prevBankPort;
	}
	public void setPrevBankPort(int prevBankPort) {
		this.prevBankPort = prevBankPort;
	}
	public int getNextBankPort() {
		return nextBankPort;
	}
	public void setNextBankPort(int nextBankPort) {
		this.nextBankPort = nextBankPort;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getTransferOutPort() {
		return transferOutPort;
	}
	public void setTransferOutPort(int transferOutPort) {
		this.transferOutPort = transferOutPort;
	}
	public int getTransferInPort() {
		return transferInPort;
	}
	public void setTransferInPort(int transferInPort) {
		this.transferInPort = transferInPort;
	}
}
