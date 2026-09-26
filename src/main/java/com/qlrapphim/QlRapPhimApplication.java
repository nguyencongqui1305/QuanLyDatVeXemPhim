package com.qlrapphim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QlRapPhimApplication {
    public static void main(String[] args) {
        // ================================================================
        // BẮT BUỘC: Set trước khi Spring khởi tạo HikariCP connection pool.
        // oracle.jdbc.defaultNChar=true => JDBC driver dùng UCS-2 (Unicode)
        // cho mọi String parameter, fix lỗi mojibake với cột NVARCHAR2.
        // ================================================================
        System.setProperty("oracle.jdbc.defaultNChar", "true");
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.stdout.encoding", "UTF-8");
        System.setProperty("sun.stderr.encoding", "UTF-8");

        SpringApplication.run(QlRapPhimApplication.class, args);
    }
}
