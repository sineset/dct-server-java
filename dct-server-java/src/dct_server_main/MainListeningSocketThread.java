package dct_server_main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainListeningSocketThread extends Thread {
	protected int port;
	protected ServerSocket main_listening_socket;
	protected AtomicBoolean executing;
	protected Date last_exception_date;

	public MainListeningSocketThread(int listen_port) {
		port = listen_port;
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
					ClientSocketThread newClient = new ClientSocketThread(client_socket);
					MainThread.clientSocketList.add(newClient);
				} catch (SocketTimeoutException et) {
					MainThread.myLogger.log("Accept client connection socket timeout exception: " + et.getMessage());
				} catch (IOException eio) {
					MainThread.myLogger.log("Accept client connection IO exception: " + eio.getMessage());
				} catch (SecurityException es) {
					MainThread.myLogger.log("Accept client connection security exception: " + es.getMessage());
				} catch (java.nio.channels.IllegalBlockingModeException et) {
					MainThread.myLogger
							.log("Accept client connection illegal blocking mode exception: " + et.getMessage());
				}
			}
			main_listening_socket.close();
		} catch (IOException eio) {
			MainThread.myLogger.log("Main listening socket thread IO exception: " + eio.getMessage() + "), exiting");
		}
	}

	public void terminate() {
		executing.set(false);
	}
}
