import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

class ClientSocketThread extends Thread {
	public enum Mode {
						IDLE, SEND, RECV, CLOS
	}

	protected Mode mode;
	protected Socket socket;
	protected InputStreamReader in_reader;
	protected OutputStreamWriter out_writer;
	protected BufferedReader buf_reader;
	protected BufferedWriter buf_writer;
	protected String recieved_line;
	protected String recieved_lines_data;
	protected MyLastException my_last_exception;
	protected AtomicBoolean execute;

	public ClientSocketThread(Socket connection_socket) throws IOException {
		mode = Mode.IDLE;
		socket = connection_socket;
		in_reader = new InputStreamReader(socket.getInputStream());
		out_writer = new OutputStreamWriter(socket.getOutputStream());
		buf_reader = new BufferedReader(in_reader);
		buf_writer = new BufferedWriter(out_writer);
		execute = new AtomicBoolean(true);
		setName(getName() + " / " + socket.getRemoteSocketAddress().toString());
		start();
	}

	@Override
	public void run() {
		recieved_lines_data = "";
		try {
			SendLine("DCT-SERER v.0.1 ready. Supported commands: DATA, GET, CLOSE");
			while (execute.get()) {
				recieved_line = buf_reader.readLine();
				if (recieved_line == null) {
					execute.set(false);
				} else if (recieved_line.compareTo("CLOSE") == 0) {
					mode = Mode.CLOS;
					SendLine("OK Bye bye");
					execute.set(false);
				} else if (recieved_line.compareTo("DATA") == 0) {
					if (mode == Mode.IDLE) {
						mode = Mode.RECV;
						SendLine("OK End data with <CR><LF>.<CR><LF>");
						recieved_lines_data = "";
					} else {
						SendLine("ERR Invalid session command sequence");
					}
				} else if (recieved_line.compareTo(".") == 0) {
					mode = Mode.IDLE;
					String decoded = Base64Converter.Decode(recieved_lines_data);
					if (decoded == null) {
						SendLine("ERR Wrong data recieved");
						MainThread.myLogger.Logging("Unable decode string " + recieved_lines_data);
					} else {
						SendLine("OK Data recieved");
						if (MainThread.myLogger.SaveToFile(MainThread.myLogger.logPath + UUID.randomUUID().toString() + ".txt", decoded) == true) {
						} else {
							MainThread.myLogger.Logging("Unable save data");
						}
					}
					recieved_lines_data = "";
				} else if (recieved_line.startsWith("RETR")) {
					if (mode == Mode.IDLE) {
						mode = Mode.SEND;
						String order;
						String[] verify = recieved_line.split(" ");
						if ((verify.length > 1) && (verify[1].length() > 0)) {
							order = verify[1];
							SendLine("Xn==" + order);
							SendLine(".");
						} else {
							SendLine("ERR Invalid order name");
						}
						mode = Mode.IDLE;
					} else {
						SendLine("ERR Invalid session command sequence");
					}
				} else {
					if (mode == Mode.RECV) {
						recieved_lines_data += recieved_line;
					} else {
						SendLine("ERR Invalid session command sequence");
					}
				}
			}
			buf_reader.close();
			buf_writer.close();
			in_reader.close();
			out_writer.close();
			socket.close();
		} catch (IOException e) {
			my_last_exception.Save(e);
			MainThread.myLogger.Logging("I/O Error in client socket: " + e.getMessage());
			try {
				buf_reader.close();
				buf_writer.close();
				socket.close();
			} catch (IOException e1) {
				my_last_exception.Save(e1);
				MainThread.myLogger.Logging("I/O Error while socket error processing : " + e1.getMessage());
			}
			MainThread.clientSocketList.remove(this);
			MainThread.myLogger.Logging("Connection " + this.toString() + "closed after error");
		}
//		MainThread.barcodesQueue.add(recieved_lines_data);
		MainThread.clientSocketList.remove(this);
	}

	private void SendLine(String send_data) throws IOException {
		buf_writer.write(send_data);
		buf_writer.newLine();
		buf_writer.flush();
	}
}
