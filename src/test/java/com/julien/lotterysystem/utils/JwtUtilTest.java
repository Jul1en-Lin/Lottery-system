package com.julien.lotterysystem.utils;

import com.julien.lotterysystem.common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@Slf4j
@SpringBootTest
public class JwtUtilTest {
    @Test
    void genJwt(){
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("userId", 1);
        System.out.println(JwtUtil.genJwt(claims));
    }

}
