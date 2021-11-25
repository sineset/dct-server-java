package dct_server_main;

import java.util.Base64;

public class Base64Converter {

	public static String Encode(String encodable) {
		Base64.Encoder my64Encoder = Base64.getEncoder();
		byte[] b_encoded = my64Encoder.encode(encodable.getBytes());
		String encoded = new String(b_encoded);
		return encoded;
	}

	public static String Decode(String decodable) {
		String decoded;
		Base64.Decoder my64Decoder = Base64.getDecoder();
		try {
			byte[] b_decoded = my64Decoder.decode(decodable);
			decoded = new String(b_decoded);
		} catch (IllegalArgumentException ea) {
			decoded = null;
		}
		return decoded;
	}

}
