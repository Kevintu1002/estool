package com.kevin;

import com.kevin.model.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EsEngineApplication {
    public static void main(String[] args) {


        SpringApplication application = new SpringApplication(EsEngineApplication.class);
        application.run(args);


        if (args != null && args.length >0){
            String configPath = args[0];
            Config.loadConfig(configPath);
        }


    }
}
