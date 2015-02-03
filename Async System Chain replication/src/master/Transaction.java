package master;
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
	public Transaction(int id, int accountNo, int balance, String type) {
		super();
		this.id = id;
		this.accountNo = accountNo;
		this.balance = balance;
		this.type = type;
	}
	
	public Transaction(int id, int accountNo, String type) {
		super();
		this.id = id;
		this.accountNo = accountNo;
		this.type = type;
	}

	int id;
	int accountNo;
	int balance;
	String type;
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
				+ getType();
	}
}
