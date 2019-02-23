package org.fkjava.common.data.utils;

import java.io.InputStream;

/**
 *
 * @author 罗文强 <luo_wenqiang@qq.com>
 */
public class SHA {

	public static String sha256(String source) {
		Checksum cs = new Checksum("SHA-256");
		return cs.hash(source);
	}

	public static String sha256(byte[] source) {
		Checksum cs = new Checksum("SHA-256");
		return cs.sum(source);
	}

	public static String sha256(InputStream in) {
		Checksum cs = new Checksum("SHA-256");
		return cs.sum(in);
	}
}
