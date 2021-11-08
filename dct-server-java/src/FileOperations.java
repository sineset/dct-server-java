import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileOperations {

	public static Boolean SaveToFile(String path, String data) {
		Charset chrst_utf8 = Charset.forName("UTF-8");
		try {
			FileWriter flwr = new FileWriter(path, chrst_utf8, true);
			flwr.write(data);
			flwr.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}
