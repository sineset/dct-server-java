import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.net.Socket;

class ClientSocketThread extends Thread {
	private Socket socket;
	private InputStreamReader in_reader;
	private OutputStreamWriter out_writer;
	private BufferedReader buf_reader;
	private BufferedWriter buf_writer;
	String word;
	String recieved_data;

	public ClientSocketThread(Socket connection_socket) throws IOException {
		socket = connection_socket;
		in_reader = new InputStreamReader(socket.getInputStream());
		out_writer = new OutputStreamWriter(socket.getOutputStream());
		buf_reader = new BufferedReader(in_reader);
		buf_writer = new BufferedWriter(out_writer);
		start();
	}

	@Override
	public void run() {
		recieved_data = "";
		try {
			while (true) {
				word = buf_reader.readLine();
				if (word == null) {
					break;
				}
				recieved_data += word;
//				for (ClientSocketThread client_sock : MainThread.clientSocketList) {
//					client_sock.send(word + "\r\n");
//				}
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
//		System.out.println(recieved_data);
		MainThread.barcodesQueue.add(recieved_data);
		MainThread.clientSocketList.remove(this);
	}

//	private void send(String msg) {
//		try {
//			buf_writer.write(msg + "\n");
//			buf_writer.flush();
//		} catch (IOException ignored) {
//		}
//	}
}
