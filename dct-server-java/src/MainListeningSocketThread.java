import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainListeningSocketThread extends Thread {
	protected int port;
	protected ServerSocket main_listening_socket;
	protected AtomicBoolean executing;
	protected Date last_exception_date;
	public MyLastException my_last_exception;

	public MainListeningSocketThread(int listen_port) {
		port = listen_port;
		my_last_exception = new MyLastException();
		setName(getName() + " / Main server socket");
		start();
	}

	@Override
	public void run() {
		try {
			main_listening_socket = new ServerSocket(port);
			AtomicBoolean execuing = new AtomicBoolean(true);
			while (execuing.get()) {
				try {
					Socket client_socket = main_listening_socket.accept();
					ClientSocketThread newsock = new ClientSocketThread(client_socket);
					MainThread.clientSocketList.add(newsock);
				} catch (Exception ee) {
					System.out.println(ee.getMessage());
					my_last_exception.Save(ee);
					MainThread.myLogger.Logging("Accept client connection error: " + ee.getMessage());
				}
			}
			main_listening_socket.close();
		} catch (Exception e) {
			System.out.println(e.toString());
			my_last_exception.Save(e);
			MainThread.myLogger.Logging("Main listening socket error: " + e.getMessage());
		}
	}

	public void terminate() {
		executing.set(false);
	}
}
