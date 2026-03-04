package com.julien.lotterysystem.utils;


import com.julien.lotterysystem.entity.response.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class JacksonUtilTest {

    @Autowired
    private ObjectMapper objectMapper;

    private String str = "";

    @Test
    void JasonTest1() {
        // Result 类序列化 jackson
        Result<String> body = Result.success("test1");
        str = objectMapper.writeValueAsString(body);
        System.out.println("Result 类序列化 jackson：" + str);

        // 反序列化
        body = objectMapper.readValue(this.str, Result.class);
        System.out.println("Result 类反序列化 jackson：" + body.getData());
    }

    @Test
    void JasonTest2() {
        // List 序列化 jackson
        List<Result<String>> body = Arrays.asList(Result.success("1"), Result.success("2"));
        str = objectMapper.writeValueAsString(body);
        System.out.println("List 序列化 jackson：" + str);

        //
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, Result.class);
        body = objectMapper.readValue(str,javaType);
        for (Result<String> result : body) {
            System.out.println(result.getData());
        }
    }
}
