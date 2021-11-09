import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	public final String logPath = "e:\\dct-svr\\";
	public final String logFile = logPath + "log.txt";
	private SimpleDateFormat date_format_string;

	public Logger() {
		date_format_string = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public String Now() {
		return date_format_string.format(new Date());
	}

	public long NowTimestamp() {
		return new Date().getTime();
	}

	public Date NowDate() {
		return new Date();
	}

	public Boolean SaveToFile(String path, String data) {
		Charset chrst_utf8 = Charset.forName("UTF-8");
		try {
			FileWriter flwr = new FileWriter(path, chrst_utf8, true);
			flwr.write(data);
			flwr.close();
			flwr = null;
		} catch (IOException e) {
			return false;
		}
		chrst_utf8 = null;
		return true;
	}

	public synchronized Boolean Logging(String data) {
		Charset chrst_utf8 = Charset.forName("UTF-8");
		try {
			CharSequence chsq = (CharSequence) ("[" + Now() + "]" + System.lineSeparator() + data);
			FileWriter flwr = new FileWriter(logFile, chrst_utf8, true);
			flwr.append(chsq + System.lineSeparator());
			flwr.close();
			flwr = null;
		} catch (IOException e) {
			return false;
		}
		chrst_utf8 = null;
		return true;
	}

	public Boolean Logging(String log_file_name, String data) {
		Charset chrst_utf8 = Charset.forName("UTF-8");
		try {
			CharSequence chsq = (CharSequence) ("[" + Now() + "]" + System.lineSeparator() + data);
			FileWriter flwr = new FileWriter(logPath + log_file_name, chrst_utf8, true);
			flwr.append(chsq);
			flwr.close();
			flwr = null;
		} catch (IOException e) {
			return false;
		}
		chrst_utf8 = null;
		return true;
	}

}
