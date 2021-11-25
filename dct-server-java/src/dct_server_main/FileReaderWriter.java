package dct_server_main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReaderWriter {

	public static String readOrderFile(String orderName) {
		String readed_data = "";
		try {
			Path pt = Paths.get(MainThread.dataDir + "\\" + orderName + ".xml");
			BufferedReader bufRead = Files.newBufferedReader(pt);
			String lnbuf;
			while ((lnbuf = bufRead.readLine()) != null) {
				readed_data += lnbuf;
			}
			bufRead.close();
			return readed_data;
		} catch (SecurityException es) {
			MainThread.myLogger
					.log("Unable read order file " + orderName + " by security exception (" + es.getMessage() + ")");
		} catch (IOException eio) {
			MainThread.myLogger
					.log("Unable read order file " + orderName + " by I/O exception (" + eio.getMessage() + ")");
		} catch (InvalidPathException eip) {
			MainThread.myLogger.log(
					"Unable read order file " + orderName + " by ivnalid path exception (" + eip.getMessage() + ")");
		}
		return null;
	}

	public static Boolean WriteFile(String fileName, String data) {
		try {
			Path pt = Paths.get(MainThread.dataDir + "\\" + fileName);
			BufferedWriter bufWrite = Files.newBufferedWriter(pt);
			bufWrite.write(data);
			bufWrite.newLine();
			bufWrite.close();
			return true;
		} catch (SecurityException es) {
			MainThread.myLogger
					.log("Unable write file " + fileName + " by security exception (" + es.getMessage() + ")");
		} catch (IOException eio) {
			MainThread.myLogger.log("Unable write file " + fileName + " by I/O exception (" + eio.getMessage() + ")");
		} catch (InvalidPathException eip) {
			MainThread.myLogger
					.log("Unable write file " + fileName + " by ivnalid path exception (" + eip.getMessage() + ")");
		} catch (IllegalArgumentException eia) {
			MainThread.myLogger
					.log("Unable write file " + fileName + " by illegal argument exception (" + eia.getMessage() + ")");
		}
		return false;
	}
}
