import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

class SocketThread extends Thread {
	private Socket socket;
	private BufferedReader buf_rd_in;
	private BufferedWriter buf_wr_out;
	String word;

	public SocketThread(Socket new_socket) throws IOException {
		socket = new_socket;
		buf_rd_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		buf_wr_out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		start();
	}

	@Override
	public void run() {

		try {
			while (true) {
				word = buf_rd_in.readLine();
				System.out.println(word);
				if (word == null) {
					break;
				}
				if (word.equals("stop")) {
					break;
				}
				for (SocketThread vr : Main.serverLst) {
					vr.send(word + "\r\n");
				}
			}
			buf_rd_in.close();
			buf_wr_out.close();
			socket.close();
			Main.serverLst.remove(this);
			System.out.println(">> Connection " + this.toString() + "closed");
		} catch (IOException e) {
			System.out.println(">>> I/O Error");
			System.out.println(e.getMessage());
			// e.printStackTrace();
			try {
				buf_rd_in.close();
				buf_wr_out.close();
				socket.close();
			} catch (IOException e1) {
				System.out.println(">>> I/O Error while error processing");
				System.out.println(e1.getMessage());
				// e1.printStackTrace();
			}
			Main.serverLst.remove(this);
			System.out.println(">> Connection " + this.toString() + "closed after error");
		}
	}

	private void send(String msg) {
		try {
			buf_wr_out.write(msg + "\n");
			buf_wr_out.flush();
		} catch (IOException ignored) {
		}
	}
}
