import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;

class ClientSocketThread extends Thread {
	public enum Mode {
		IDLE, SEND, RECV, CLOS
	}

	private Mode mode;
	private Socket socket;
	private InputStreamReader in_reader;
	private OutputStreamWriter out_writer;
	private BufferedReader buf_reader;
	private BufferedWriter buf_writer;
	private String word_str;
	private String recieved_data_str;
	private String terminal_id;

	public ClientSocketThread(Socket connection_socket) throws IOException {
		terminal_id = "";
		mode = Mode.IDLE;
		socket = connection_socket;
		in_reader = new InputStreamReader(socket.getInputStream());
		out_writer = new OutputStreamWriter(socket.getOutputStream());
		buf_reader = new BufferedReader(in_reader);
		buf_writer = new BufferedWriter(out_writer);
		start();
	}

	@Override
	public void run() {
		recieved_data_str = "";
		boolean cycle = true;
		try {
			while (cycle) {
				word_str = buf_reader.readLine();
				if (word_str == null) {
					cycle = false;
				} else if (word_str.compareTo("DATA") == 0) {
					if (mode == Mode.IDLE) {
						mode = Mode.RECV;
						buf_writer.write("OK End data with \".\" at new line\n");
						buf_writer.flush();
						recieved_data_str = "";
					} else {
						buf_writer.write("ERR Invalid session command sequence\n");
						buf_writer.flush();
					}
				} else if (word_str.compareTo(".") == 0) {
					mode = Mode.IDLE;
					buf_writer.write("OK Data recieved\n");
					buf_writer.flush();
					System.out.println(recieved_data_str);
					recieved_data_str = "";
				} else if (word_str.compareTo("GETDATA") == 0) {
					mode = Mode.SEND;
					buf_writer.write("XmGhjKJkJhnxdfghxdg34655678tjHFDhg57==\n");
					buf_writer.flush();
					buf_writer.write("ENDDATA\n");
					buf_writer.flush();
				} else if (word_str.compareTo("CLOSE") == 0) {
					mode = Mode.CLOS;
					buf_writer.write("OK Bye bye\n");
					buf_writer.flush();
					System.out.println("*session closed*");
					cycle = false;
				} else if (word_str.startsWith("EHLO")) {
					if (mode == Mode.IDLE) {
						String[] ehlo = word_str.split(" ");
						terminal_id = ehlo[1];
						if (terminal_id.length() > 0) {
							buf_writer.write("OK\n");
							buf_writer.flush();
						} else {
							buf_writer.write("ERR Invalid terminal id\n");
							buf_writer.flush();
						}
					} else {
						buf_writer.write("ERR Invalid session command sequence\n");
						buf_writer.flush();
					}
				} else {
					if (mode == Mode.RECV) {
						recieved_data_str += word_str;
					} else {
						buf_writer.write("ERR Invalid session command sequence\n");
						buf_writer.flush();
					}
				}
			}
			buf_reader.close();
			buf_writer.close();
			in_reader.close();
			out_writer.close();
			socket.close();
		} catch (IOException e) {
			System.out.println(">>> I/O Error in socket");
			System.out.println(e.getMessage());
			try {
				buf_reader.close();
				buf_writer.close();
				socket.close();
			} catch (IOException e1) {
				System.out.println(">>> I/O Error while error processing n socket");
				System.out.println(e1.getMessage());
			}
			MainThread.clientSocketList.remove(this);
			System.out.println(">> Connection " + this.toString() + "closed after error");
		}
//		System.out.println(recieved_data_str);
//		MainThread.barcodesQueue.add(recieved_data_str);
		MainThread.clientSocketList.remove(this);
	}

//	for (ClientSocketThread client_sock : MainThread.clientSocketList) {
//	client_sock.send(word_str + "\r\n");
//}

//	private void send(String msg) {
//		try {
//			buf_writer.write(msg + "\n");
//			buf_writer.flush();
//		} catch (IOException ignored) {
//		}
//	}
}
