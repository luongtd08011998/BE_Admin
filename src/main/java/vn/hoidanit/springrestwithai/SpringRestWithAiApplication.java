package vn.hoidanit.springrestwithai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(VnptPortalProperties.class)
public class SpringRestWithAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRestWithAiApplication.class, args);
	}

}
