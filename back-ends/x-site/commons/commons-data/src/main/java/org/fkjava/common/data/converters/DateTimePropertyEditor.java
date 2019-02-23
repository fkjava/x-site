package org.fkjava.common.data.converters;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimePropertyEditor extends PropertyEditorSupport {

	// 第一个转换器能够转换，那么就直接返回；如果第一个不能转换就尝试第二个
	private static final SimpleDateFormat[] FORMATS = new SimpleDateFormat[] {
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), //
			new SimpleDateFormat("yyyy-MM-dd HH:mm"), //
			new SimpleDateFormat("yyyy-MM-dd HH"), //
			new SimpleDateFormat("yyyy-MM-dd")//
	};

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		for (SimpleDateFormat format : FORMATS) {
			try {
				Date date = format.parse(text);
				super.setValue(date);
				return;
			} catch (ParseException e) {
				// 不要处理，直接继续循环下一个
			}
		}
		throw new IllegalArgumentException("字符串 " + text + " 无法转换为Date");
	}
}
