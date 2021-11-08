import java.util.Base64;

public class Base64Converter {

	public static Boolean Encode(String encodable, String encoded) {
		try {
			Base64.Encoder my64Encoder = Base64.getEncoder();
			byte[] b_encoded = my64Encoder.encode(encodable.getBytes());
			encoded = new String(b_encoded);
			return true;
		} catch (Exception ed) {
			return false;
		}
	}

	public static Boolean Decode(String decodable, String decoded) {
		try {
			Base64.Decoder my64Decoder = Base64.getDecoder();
			byte[] b_decoded = my64Decoder.decode(decodable);
			decoded = new String(b_decoded);
			return true;
		} catch (Exception ed) {
			return false;
		}
	}

}
