package dct_server_main;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private String logDir;
	private String mainLogFile;
	protected SimpleDateFormat date_format_string;

	public Logger(String logdir) {
		date_format_string = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		logDir = logdir;
		mainLogFile = logDir + "\\log.txt";
	}

	public String now() {
		return date_format_string.format(new Date());
	}

	public long nowTimestamp() {
		return new Date().getTime();
	}

	public Date nowDate() {
		return new Date();
	}

	public synchronized Boolean log(String data) {
		try {
			Charset chrst_utf8 = Charset.forName("UTF-8");
			CharSequence chsq = (CharSequence) ("[" + now() + "]" + System.lineSeparator() + data);
			FileWriter flwr = new FileWriter(mainLogFile, chrst_utf8, true);
			flwr.append(chsq + System.lineSeparator());
			flwr.close();
			flwr = null;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public Boolean log(String log_file_name, String data) {
		Charset chrst_utf8 = Charset.forName("UTF-8");
		try {
			CharSequence chsq = (CharSequence) ("[" + now() + "]" + System.lineSeparator() + data);
			FileWriter flwr = new FileWriter(logDir + "\\" + log_file_name, chrst_utf8, true);
			flwr.append(chsq);
			flwr.close();
			flwr = null;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
