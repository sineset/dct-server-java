package dct_server_main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainListeningThread extends Thread {
	protected int port;
	protected ServerSocket main_listening_socket;
	protected AtomicBoolean executing;

	public MainListeningThread(int listen_port) {
		port = listen_port;
		main_listening_socket = null;
		executing = new AtomicBoolean(true);
		setName(getName() + " / Main listening socket thread");
		start();
	}

	@Override
	public void run() {
		try {
			main_listening_socket = new ServerSocket(port);
			while (executing.get() && MainThread.global_executing.get()) {
				Socket client_socket = main_listening_socket.accept();
				ClientConnectionThread newClient = new ClientConnectionThread(client_socket);
				MainThread.clientSocketList.add(newClient);
			}
			main_listening_socket.close();
		} catch (SocketTimeoutException et) {
			MainThread.myLogger.log(
					"Fatal: thread \"" + getName() + "\" socket timeout exception (" + et.getMessage() + "), exiting");
		} catch (IOException eio) {
			MainThread.myLogger
					.log("Fatal: thread \"" + getName() + "\" IO exception: " + eio.getMessage() + "), exiting");
		} catch (SecurityException es) {
			MainThread.myLogger
					.log("Fatal: thread \"" + getName() + "\" security exception: " + es.getMessage() + "), exiting");
		} catch (java.nio.channels.IllegalBlockingModeException et) {
			MainThread.myLogger.log("Fatal: thread \"" + getName() + "\" illegal blocking mode exception: " +
					et.getMessage() + "), exiting");
		}
		if (main_listening_socket != null) {
			try {
				main_listening_socket.close();
			} catch (IOException eio) {
				MainThread.myLogger.log("Thread \"" + getName() + "\" exiting IO exception (" + eio.getMessage() + ")");
			}
		}
	}

	public void terminate() {
		executing.set(false);
	}
}
