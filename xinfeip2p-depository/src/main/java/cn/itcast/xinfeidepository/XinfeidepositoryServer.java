package cn.itcast.xinfeidepository;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 心飞银行存管系统
 */
@SpringBootApplication
@MapperScan("cn.itcast.xinfeidepository.mapper")
public class XinfeidepositoryServer {
    public static void main(String[] args) {
        SpringApplication.run(XinfeidepositoryServer.class, args);
    }
}
