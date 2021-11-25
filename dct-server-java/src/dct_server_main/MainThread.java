package dct_server_main;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainThread {
	public static final int PORT = 4004;
	protected static MainListeningThread mySocketThread;
	public static ArrayList<ClientConnectionThread> clientSocketList = new ArrayList<ClientConnectionThread>();
	// public static ArrayList<DBThread> dbList = new ArrayList<DBThread>();
	public static ConcurrentLinkedQueue<String> barcodesQueue = new ConcurrentLinkedQueue<String>();
	public static Logger myLogger = null;
	public static String dataDir = "";
	public static String logDir = "";
	public static AtomicBoolean global_executing = new AtomicBoolean(true);

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--data-dir"))
				dataDir = args[i + 1];
			if (args[i].equals("--log-dir"))
				logDir = args[i + 1];
		}
		if (logDir == "") {
			System.out.print("Fatal: log dir not defined, exiting");
			global_executing.set(false);
			return;
		}
		if (dataDir == "") {
			System.out.print("Fatal: data dir not defined, exiting");
			global_executing.set(false);
			return;
		}
		myLogger = new Logger(logDir);
		// dbList.add(new DBThread("server.net.local", 1433, "fab", "sa", "qwerty"));
		mySocketThread = new MainListeningThread(4004);
		while (global_executing.get()) {
			if (mySocketThread.isAlive() != true) {
				// System.out.println("My socket exited:");
				// System.out.println(mySocketThread.my_last_exception.getException().getMessage());
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException ei) {
				myLogger.log("Main thread sleep error: " + ei.getMessage());
				global_executing.set(false);
			}
		}
	}
}
