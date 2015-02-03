Phase 4
Asynchronous Systems
Nikhil Ambekar	(109819116)
Akhil Tiwari	(109748386)


Config files are kept in config folder
BankInfo.txt		-- Initial Bank Chain Config for Master
ClientRandom.txt	-- Probability Distribution function
ClientPorts.txt		-- Banks to which Client should Communicate
ClientReq.txt		-- Request file for Each Client
ports.txt		-- Server configuration file
ports1fail.txt		-- Server configuration file with head failure
ports2fail.txt		-- Server configuration file with tail failure followed by head failure



Running the code
a)Compile Master as
javac -cp log4j-1.2.17.jar; FailureObj.java Bank.java Master.java

b)Run Master as
java -cp log4j-1.2.17.jar; Master

c)Compile Server as 
javac -cp log4j-1.2.17.jar; InitialStart.java

d)Run Server as
java -cp log4j-1.2.17.jar; InitialStart ports.txt

e)Compile Client as
javac -cp log4j-1.2.17.jar Transaction.java ChainClientR.java ClientThread.java

f)Run Client as
java -cp log4j-1.2.17.jar; ClientThread ClientPorts.txt

Initially We create 3 bank chains communicated in fashion 1-2-3
Each Bank receives set of initial requests which are addressed by respective banks.
Transfer request are sent to Destination Banks for Deposit and reply is received.


General Tests

i)		Duplicate Request	: Resend the reply as Already Processed
ii)		Insufficient Funds	: Send the reply as Invalid Request
iii)		Head Failure		: Again resend the request to the next bank regarding transfers
iv)		Tail Failure		: Resend the request in sent to the next bank regarding transfers
