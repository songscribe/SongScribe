package songscribe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebServer {
    public static void main(String[] args) {
        SpringApplication.run(WebServer.class, args);
//        SpringApplicationBuilder builder = new SpringApplicationBuilder(WebServer.class);
//        builder.headless(false);
//        builder.run(args);
    }
}
