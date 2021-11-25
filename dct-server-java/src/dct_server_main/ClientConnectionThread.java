package dct_server_main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class ClientConnectionThread extends Thread {
	protected Socket socket;
	protected InputStreamReader in_reader;
	protected OutputStreamWriter out_writer;
	protected AtomicBoolean executing;
	protected char[] recv_buffer;
	protected int recv_chars_number;
	protected String recv_string;
	protected ArrayList<String> recv_strings_list;
	protected ArrayList<String> send_strings_list;
	protected long prev_cycle_millis;

	public ClientConnectionThread(Socket connection_socket) {
		socket = connection_socket;
		in_reader = null;
		out_writer = null;
		recv_buffer = new char[1024];
		recv_string = "";
		recv_strings_list = new ArrayList<String>();
		send_strings_list = new ArrayList<String>();
		executing = new AtomicBoolean(true);
		prev_cycle_millis = System.currentTimeMillis();
		setName(getName() + " " + socket.getRemoteSocketAddress().toString());
		start();
	}

	protected void finalize() {
		executing.set(false);
		MainThread.myLogger.log("Thread \"" + getName() + "\" finalized by GC");
	}

	@Override
	public void run() {
		try {
			in_reader = new InputStreamReader(socket.getInputStream());
			out_writer = new OutputStreamWriter(socket.getOutputStream());
			while (executing.get() && MainThread.global_executing.get()) {
				checkConnection();
				recieveData();
				processingStringList();
				sendData();
				Thread.sleep(1);
			}
		} catch (IOException eio) {
			MainThread.myLogger
					.log("Thread \"" + getName() + "\" I/O exception (" + eio.getMessage() + "), thread exiting");
			executing.set(false);
		} catch (InterruptedException eir) {
			MainThread.myLogger
					.log("Thread \"" + getName() + "\" interrupt exception (" + eir.getMessage() + "), thread exiting");
			executing.set(false);
		}
		if (in_reader != null)
			try {
				in_reader.close();
			} catch (IOException eio) {
				MainThread.myLogger.log("Thread \"" + getName() + "\" exiting I/O exception (" + eio.getMessage());
			}
		if (out_writer != null)
			try {
				out_writer.close();
			} catch (IOException eio) {
				MainThread.myLogger.log("Thread \"" + getName() + "\" exiting I/O exception (" + eio.getMessage());
			}
		if (socket != null)
			try {
				socket.close();
			} catch (IOException eio) {
				MainThread.myLogger.log("Thread \"" + getName() + "\" exiting I/O exception (" + eio.getMessage());
			}
		MainThread.clientSocketList.remove(this);
	}

	protected void checkConnection() throws IOException {
		if ((System.currentTimeMillis() - prev_cycle_millis) > 500) {
			out_writer.write('\0');
			out_writer.flush();
			prev_cycle_millis = System.currentTimeMillis();
		}
	}

	protected void sendData() throws IOException {
		if (send_strings_list.size() > 0) {
			for (String s_str : send_strings_list) {
				out_writer.write(s_str + System.lineSeparator());
				out_writer.flush();
			}
			send_strings_list.clear();
		}
	}

	protected void recieveData() throws IOException {
		while (in_reader.ready()) {
			recv_chars_number = in_reader.read(recv_buffer);
			for (int i = 0; i < recv_chars_number; i++) {
				if (recv_buffer[i] == '\0') {
					continue;
				} else if (recv_buffer[i] == '\r') {
					continue;
				} else if (recv_buffer[i] == '\n') {
					recv_strings_list.add(recv_string);
					recv_string = "";
				} else {
					recv_string += recv_buffer[i];
				}
			}
		}
	}

	protected void processingStringList() {
		for (String s : recv_strings_list) {
			if (s.startsWith("DATA")) {
				String[] data_arr = s.split(" ");
				if (data_arr.length == 2) {
					String data_r = Base64Converter.Decode(data_arr[1]);
					if (data_r == null) {
						send_strings_list.add("ERR Your data is invalid Base64 string");
					} else {
						send_strings_list.add("OK Data received");
						System.out.println(data_r);
					}
				} else {
					send_strings_list.add("ERR Data should be one Base64 string after command");
				}
			} else if (s.startsWith("GETORDER")) {
				String[] data_arr = s.split(" ");
				if (data_arr.length >= 2) {
					String data_r = Base64Converter.Decode(data_arr[1]);
					if (data_r == null) {
						send_strings_list.add("ERR order name is invalid Base64 string");
					} else {
						String orderData = FileReaderWriter.readOrderFile(data_r);
						if (orderData != null) {
							send_strings_list.add("DATA " + Base64Converter.Encode(orderData));
						} else {
							send_strings_list.add("ERR Unable read order");
						}
					}
				} else {
					send_strings_list.add("ERR Data should be one Base64 string after command");
				}
			} else if (s.compareTo("TIME") == 0) {
				send_strings_list.add("UTC " + Instant.now().toString());
			} else if (s.compareTo("CLOSE") == 0) {
				send_strings_list.add("OK bye bye");
				executing.set(false);
			} else {
				send_strings_list.add("ERR Invalid, not supported or not implemented command");
			}
		}
		recv_strings_list.clear();
	}
}
