import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {

	public static final int PORT = 4004;
	public static ArrayList<SocketThread> serverLst = new ArrayList<SocketThread>();

	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(PORT);
		try {
			while (true) {
				Socket socket = server.accept();
				SocketThread newsock = null;
				try {
					newsock = new SocketThread(socket);
					serverLst.add(newsock);
					System.out.println("Sockets:" + serverLst.size());
				} catch (IOException e) {
					socket.close();
					for (int i = 0; i < serverLst.size(); i++) {
						if (serverLst.get(i) == newsock) {
							serverLst.remove(i);
						}
					}
				}
			}
		} finally {
			server.close();
		}
	}
}
