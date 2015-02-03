package server;
import java.io.Serializable;

public class Transaction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * @param id
	 * @param balance
	 * @param accountNo
	 * @param type
	 */
	public Transaction(int id, int accountNo, int balance, int destaccountNo, String type, String status) {
		super();
		this.id = id;
		this.accountNo = accountNo;
		this.destaccountNo = destaccountNo;
		this.balance = balance;
		
		this.type = type;
		this.status = status;
	}
	
	public Transaction(int id, int accountNo, String type) {
		super();
		this.id = id;
		this.accountNo = accountNo;
		this.type = type;
	}

	public Transaction(int id, int accountNo,int balance,String type) {
		super();
		this.id = id;
		this.accountNo = accountNo;
		this.type = type;
		this.balance = balance;
	}
	int id;
	int accountNo;
	int balance;
	int transferbalance;
	int destaccountNo;
	int initialBal;
	String type;
	String status;
	int clientPort;

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(int accountNo) {
		this.accountNo = accountNo;
	}
	
	public String toString() {
		return "Id = " + getId() + " Account No = " + getAccountNo() + " Balance = " + getBalance() + " Action = "
				+ getType() + ((getStatus() == null)?"":" Status = "+ getStatus()) + ((getInitialBal() == 0)?"":" InitialBal = "+ getInitialBal());
	}

	public int getDestaccountNo() {
		return destaccountNo;
	}

	public void setDestaccountNo(int destaccountNo) {
		this.destaccountNo = destaccountNo;
	}

	public int getTransferbalance() {
		return transferbalance;
	}

	public void setTransferbalance(int transferbalance) {
		this.transferbalance = transferbalance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getInitialBal() {
		return initialBal;
	}

	public void setInitialBal(int initialBal) {
		this.initialBal = initialBal;
	}
}
