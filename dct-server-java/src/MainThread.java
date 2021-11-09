import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainThread {
	public static final int PORT = 4004;
	protected static MainListeningSocketThread mySocketThread;
	public static ArrayList<ClientSocketThread> clientSocketList = new ArrayList<ClientSocketThread>();
	// public static ArrayList<DBThread> dbList = new ArrayList<DBThread>();
	public static ConcurrentLinkedQueue<String> barcodesQueue = new ConcurrentLinkedQueue<String>();
	public static Logger myLogger = new Logger();

	public static void main(String[] args) {
		// dbList.add(new DBThread("server.net.local", 1433, "fab", "sa", "qwerty"));
		mySocketThread = new MainListeningSocketThread(4004);
		AtomicBoolean execuing = new AtomicBoolean(true);
		while (execuing.get()) {
			if (mySocketThread.isAlive() != true) {
//				System.out.println("My socket exited:");
//				System.out.println(mySocketThread.my_last_exception.getException().getMessage());
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				myLogger.Logging("Main thread sleep error: " + e.getMessage());
			}
		}
	}
}
