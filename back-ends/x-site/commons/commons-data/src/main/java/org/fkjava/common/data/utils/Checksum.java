package org.fkjava.common.data.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author 罗文强 <luo_wenqiang@qq.com>
 */
public class Checksum {

	private static final Charset CHARSET = Charset.forName("UTF-8");

	private final Builder builder;

	public Checksum(String algorithm) {
		builder = builder(algorithm);
	}

	/**
	 * 把字符串转换为MD5散列数据摘要，通常用于密码验证。
	 *
	 * @param source
	 * @return 密文。把明文散列后得到的数字转换为16进制格式的字符串
	 */
	public String hash(String source) {
		return builder.update(source.getBytes(CHARSET)).sum();
	}

	/**
	 * 对字节数组计算校验和，通常用于用于判断文件内容是否相同。
	 *
	 * @param source
	 * @return
	 */
	public String sum(byte[] source) {
		return builder.update(source).sum();
	}

	/**
	 * 对输入流里面的内容生成指纹，使用此方法的时候要注意：流只能被使用一次！
	 *
	 * @param in
	 * @return
	 */
	public String sum(InputStream in) {
		try {
			byte[] buf = new byte[1024];
			for (int i = in.read(buf); i != -1; i = in.read(buf)) {
				builder.update(buf, 0, i);
			}
			return builder.sum();
		} catch (IOException e) {
			throw new UncheckedIOException("无法计算输入流的指纹", e);
		}
	}

	public static Builder builder(String algorithm) {
		return new Builder(algorithm);
	}

	public static class Builder {
		private MessageDigest messageDigest;

		// 构建数据摘要算法
		public Builder(String algorithm) {
			try {
				messageDigest = MessageDigest.getInstance(algorithm);
				messageDigest.reset();
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("不支持的算法" + algorithm + "，" + e.getLocalizedMessage(), e);
			}
		}

		/**
		 * 把字节数组更新到数据摘要里面
		 * 
		 * @param data
		 * @param offset
		 * @param len
		 * @return
		 */
		public Builder update(byte[] data, int offset, int len) {
			messageDigest.update(data, offset, len);
			return this;
		}

		/**
		 * 把字节数组更新到数据摘要里面
		 * 
		 * @param data
		 * @return
		 */
		public Builder update(byte[] data) {
			update(data, 0, data.length);
			return this;
		}

		/**
		 * 计算数据摘要的结果，转换为十六进制字符串
		 * 
		 * @return
		 */
		public String sum() {
			byte[] digest = messageDigest.digest();
			String result = ByteUtils.toHex(digest);
			return result;
		}
	}
}
