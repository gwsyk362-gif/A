package assistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@org.mybatis.spring.annotation.MapperScan("assistant.mapper")

@SpringBootApplication
public class assistant_app {

    public static void main(String[] args) {

        SpringApplication.run(assistant_app.class, args);
        System.out.println("---个性化学习助手已成功启动 ---");
    }
}