package master;

import java.io.Serializable;
import java.util.ArrayList;

public class Bank implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Integer> members = new ArrayList<Integer>();
	private int headDatagramPort;
	private int tailDatagramPort;
	private int tailTransferIn;
	private int tailTransferOut;
	private int bankNumber;
	
	
	public void removeElement(Integer port){
		members.remove(port);
	}
	
	public ArrayList<Integer> getMembers() {
		return members;
	}
	public void setMembers(ArrayList<Integer> members) {
		this.members = members;
	}
	public int getHeadDatagramPort() {
		return headDatagramPort;
	}
	public void setHeadDatagramPort(int headDatagramPort) {
		this.headDatagramPort = headDatagramPort;
	}
	public int getTailDatagramPort() {
		return tailDatagramPort;
	}
	public void setTailDatagramPort(int tailDatagramPort) {
		this.tailDatagramPort = tailDatagramPort;
	}
	public int getTailTransferIn() {
		return tailTransferIn;
	}
	public void setTailTransferIn(int tailTransferIn) {
		this.tailTransferIn = tailTransferIn;
	}
	public int getTailTransferOut() {
		return tailTransferOut;
	}
	public void setTailTransferOut(int tailTransferOut) {
		this.tailTransferOut = tailTransferOut;
	}

	public int getBankNumber() {
		return bankNumber;
	}

	public void setBankNumber(int bankNumber) {
		this.bankNumber = bankNumber;
	}
}
