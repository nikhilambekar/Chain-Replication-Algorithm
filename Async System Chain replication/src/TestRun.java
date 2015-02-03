import java.util.UUID;
import org.apache.log4j.Logger;

public class TestRun {
	static Logger logger = Logger.getLogger(TestRun.class);

	public static final void main(String... aArgs) {

		// generate random UUIDs
		UUID idOne = UUID.randomUUID();
		UUID idTwo = UUID.randomUUID();
		logger.info("UUID One: " + idOne);
		logger.debug("UUID Two: " + idTwo);
	}

}