package org.fkjava.common.data.utils;

/**
 * 负责在byte和十六进制字符串之间进行转换的一个工具
 *
 * @author luowenqiang
 */
public class ByteUtils {

	private static final char[] CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F' };

	/**
	 * 把字节数组转换成十六进制字符串，两个字符表示一个字节。
	 *
	 * @param b
	 * @return
	 */
	public static String toHex(byte[] b) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xFF;
			if (v < 16) {
				builder.append(0);
			}
			builder.append(Integer.toHexString(v));
		}
		String result = builder.toString();
		result = result.toUpperCase();
		return result;
	}

	/**
	 * 把十六进制字符串还原成，字符串中每两个字符表示一个字节
	 *
	 * @param hexString
	 * @return
	 */
	public static byte[] toByteArray(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		for (int i = 0; i < CHARS.length; i++) {
			if (c == CHARS[i]) {
				return (byte) i;
			}
		}
		return Byte.MIN_VALUE;
	}
}
