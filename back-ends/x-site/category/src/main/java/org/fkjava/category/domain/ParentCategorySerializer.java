package org.fkjava.category.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ParentCategorySerializer extends JsonSerializer<Category> {

    @Override
    public void serialize(Category value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 序列化的时候什么都不写，就是为了避免把上级节点也序列化出去！
        gen.writeNull();
    }
}
