import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;

import org.w3c.dom.Document;

public class DBThread extends Thread {

	boolean execute;
	String dbserver;
	int dbPort;
	String dbName;
	String dbUser;
	String dbPassword;
	String connectionUrl;
	Connection conn;
	PreparedStatement stmt;

	public DBThread(String db_server, int db_port, String db_name, String db_user, String db_pass) {
		dbserver = db_server;
		dbPort = db_port;
		dbName = db_name;
		dbUser = db_user;
		dbPassword = db_pass;
		execute = true;
		conn = null;
		stmt = null;
		start();
	}

	public void run() {
		int connect_result = connect();
		if (connect_result != 0) {
			System.out.print("SQL connect error " + connect_result + ", unable to execute, exiting db connection");
			return;
		}
		ArrayList<ArrayList<String>> parsed_barcodes = null;
		while (execute) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.out.println("BDThread " + this.getName() + " failed sleeping: " + e.getMessage());
			}
			String _xml = MainThread.barcodesQueue.poll();
			if (_xml != null) {
				BarcodeParser myParser = new BarcodeParser();
				Document myDoc = myParser.parse(_xml);
				parsed_barcodes = myParser.sql_formatter_acceptance(myDoc);
				if (parsed_barcodes.size() > 0) {
					System.out.println("insert returned " + insert(parsed_barcodes));
				}

			}

		}
		disconnect();
	}

	public void terminate() {
		execute = false;
	}

	public int connect() {
		connectionUrl = "jdbc:sqlserver://" + dbserver + ":" + dbPort + ";databaseName=" + dbName + ";user=" + dbUser
				+ ";password=" + dbPassword;
		try {
			conn = DriverManager.getConnection(connectionUrl);
		} catch (SQLException e) {
			System.out.println("> SQL connection error, closing now: " + e.getMessage());
			try {
				conn.close();
			} catch (SQLException ec) {
				System.out.println("> closing connection error: " + ec.getMessage());
			} catch (NullPointerException en) {
				System.out.println("> Connection empty: " + en.getMessage());
			}
			return 1;
		}
		return 0;
	}

	public int disconnect() {
		if (stmt != null) {
			try {
				stmt.cancel();
			} catch (SQLFeatureNotSupportedException e) {
				System.out.println("> SQL preapred statement doesn't support cancelling: " + e.getMessage());
			} catch (SQLException e) {
				System.out.println("> SQL preapred statement already cancelled: " + e.getMessage());
			}
			try {
				stmt.close();
			} catch (SQLException e) {
				System.out.println("> SQL connection closing error: " + e.getMessage());
			}
		}
		return 0;
	}

	public int insert(ArrayList<ArrayList<String>> _barcodes) {
		if (_barcodes.size() < 1) {
			return 1;
		}
		String SQLQuery = "INSERT INTO dct_warehouse(datetime,terminal_id,operation,barcode) VALUES (SYSDATETIME(),?,?,?)";
		try {
			stmt = conn.prepareStatement(SQLQuery);
			for (int i = 0; i < _barcodes.size(); i++) {
				stmt.setString(1, _barcodes.get(i).get(0));
				stmt.setString(2, _barcodes.get(i).get(1));
				stmt.setString(3, _barcodes.get(i).get(2));
				stmt.addBatch();
			}
		} catch (SQLException e) {
			System.out.println("> Statement preparing error: " + e.getMessage());
			return 2;
		}
		try {
			int[] rs = stmt.executeBatch();
			for (int i = 0; i < rs.length; i++)
				System.out.print(rs[i] + " / ");
		} catch (SQLException e) {
			System.out.println("> Statement batch executing error: " + e.getMessage());
			return 3;
		}
		return 0;
	}
}
