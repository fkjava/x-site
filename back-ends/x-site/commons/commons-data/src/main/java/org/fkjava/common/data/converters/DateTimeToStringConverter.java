package org.fkjava.common.data.converters;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.util.StdConverter;

// 第一个泛型参数：输入的类型
// 第二个泛型参数：输出的类型
public class DateTimeToStringConverter extends StdConverter<Date, String> {

    static final private SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public String convert(Date value) {
        return FORMAT.format(value);
    }
}
