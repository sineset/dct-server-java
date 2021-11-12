package dct_server_main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class ClientSocketThread extends Thread {
	protected Socket socket;
	protected InputStreamReader in_reader;
	protected OutputStreamWriter out_writer;
	protected AtomicBoolean execute_run;
	protected char[] recv_buffer;
	protected int recv_chars_number;
	protected String recv_string;
	protected ArrayList<String> recv_strings_list;
	protected ArrayList<String> send_strings_list;
	protected long prev_cycle_millis;

	public ClientSocketThread(Socket connection_socket) {
		socket = connection_socket;
		recv_buffer = new char[1024];
		recv_string = "";
		recv_strings_list = new ArrayList<String>();
		send_strings_list = new ArrayList<String>();
		execute_run = new AtomicBoolean(true);
		prev_cycle_millis = System.currentTimeMillis();
		setName(getName() + " " + socket.getRemoteSocketAddress().toString());
		start();
	}

	protected void finalize() {
		execute_run.set(false);
		MainThread.myLogger.log("Thread \"" + getName() + "\" finalized by GC");
		MainThread.clientSocketList.remove(this);
	}

	@Override
	public void run() {
		try {
			in_reader = new InputStreamReader(socket.getInputStream());
			out_writer = new OutputStreamWriter(socket.getOutputStream());
			while (execute_run.get()) {
				if ((System.currentTimeMillis() - prev_cycle_millis) > 500) {
					out_writer.write('\0');
					out_writer.flush();
					prev_cycle_millis = System.currentTimeMillis();
				}
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
				processingStringList();
				if (send_strings_list.size() > 0) {
					for (String s_str : send_strings_list) {
						out_writer.write(s_str + System.lineSeparator());
						out_writer.flush();
					}
					send_strings_list.clear();
				}
				Thread.sleep(1);
			}
			in_reader.close();
			out_writer.close();
			socket.close();
		} catch (IOException eio) {
			MainThread.myLogger.log("IO exception (" + eio.getMessage() + ") occured from "
					+ socket.getRemoteSocketAddress().toString() + " in thread \"" + getName() + "\", thread exiting");
			execute_run.set(false);
		} catch (InterruptedException ei) {
			MainThread.myLogger.log("Interrupt exception (" + ei.getMessage() + ") occured in thread \"" + getName()
					+ "\", thread exiting");
			execute_run.set(false);
		}
	}

	protected void processingStringList() {
		for (String s : recv_strings_list) {
			if (s.startsWith("DATA")) {
				String[] data_arr = s.split(" ");
				if (data_arr.length != 2) {
					send_strings_list.add("ERR Invalid DATA command - data is empty");
				} else {
					String data_r = Base64Converter.Decode(data_arr[1]);
					if (data_r == null) {
						send_strings_list.add("ERR Your data is invalid Base64 string");
					} else {
						send_strings_list.add("OK Data recieved");
						System.out.println(data_r);
					}
				}
			} else if (s.startsWith("GET")) {
				String[] data_arr = s.split(" ");
				if (data_arr.length != 2) {
					send_strings_list.add("ERR Invalid GET command - data is empty");
				} else {
					String data_r = Base64Converter.Decode(data_arr[1]);
					if (data_r == null) {
						send_strings_list.add("ERR Your get is invalid Base64 string");
					} else {
						send_strings_list.add("OK Get recieved");
						System.out.println(data_r);
					}
				}
			} else if (s.compareTo("CLOSE") == 0) {
				send_strings_list.add("OK bye bye");
				execute_run.set(false);
			} else {
				send_strings_list.add("ERR Invalid, not supported or not implemented command");
			}
		}
		recv_strings_list.clear();
	}

}
