package com.yysystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.yysystem")
@MapperScan({"com.yysystem.modules.*.mapper", "com.yysystem.modules.*.*.mapper"})
public class YySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(YySystemApplication.class, args);
    }

}
