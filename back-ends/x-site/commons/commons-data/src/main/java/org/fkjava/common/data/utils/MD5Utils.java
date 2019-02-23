package org.fkjava.common.data.utils;

import java.io.InputStream;

/**
 *
 * @author LuoWenqiang
 */
public class MD5Utils {

	public static String hash(String s) {
		Checksum cs = new Checksum("MD5");
		return cs.hash(s);
	}

	public static String hash(byte[] source) {
		Checksum cs = new Checksum("MD5");
		return cs.sum(source);
	}

	public static String hash(InputStream in) {
		Checksum cs = new Checksum("MD5");
		return cs.sum(in);
	}

	public static void main(String[] args) {
		System.out.println(MD5Utils.hash("1234"));
	}
}
