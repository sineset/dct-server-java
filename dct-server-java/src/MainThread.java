import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainThread {

	public static final int PORT = 4004;
	protected static ArrayList<MainListeningSocketThread> listeningSocketList = new ArrayList<MainListeningSocketThread>();
	public static ArrayList<ClientSocketThread> clientSocketList = new ArrayList<ClientSocketThread>();
	public static ArrayList<DBThread> dbList = new ArrayList<DBThread>();
	public static ConcurrentLinkedQueue<String> barcodesQueue = new ConcurrentLinkedQueue<String>();

	public static void main(String[] args) throws IOException {
		dbList.add(new DBThread("server.net.local", 1433, "fab", "sa", "qwerty"));
		ServerSocket listening_socket = new ServerSocket(PORT);
		try {
			while (true) {
				Socket client_socket = listening_socket.accept();
				ClientSocketThread newsock = null;
				try {
					newsock = new ClientSocketThread(client_socket);
					clientSocketList.add(newsock);
				} catch (IOException e) {
					client_socket.close();
					for (int i = 0; i < clientSocketList.size(); i++) {
						if (clientSocketList.get(i) == newsock) {
							clientSocketList.remove(i);
						}
					}
				}
			}
		} finally {
			listening_socket.close();
		}
	}
}
