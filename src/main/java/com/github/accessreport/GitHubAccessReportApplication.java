package com.github.accessreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GitHubAccessReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitHubAccessReportApplication.class, args);
    }

}
