package com.youtubeagent.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${SPRING_DATASOURCE_URL:#{null}}")
    private String rawUrl;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        String url = rawUrl;
        if (url != null && !url.startsWith("jdbc:")) {
            url = "jdbc:" + url;
        }

        if (url != null) {
            config.setJdbcUrl(url);
        } else {
            config.setJdbcUrl("jdbc:h2:mem:youtube_agent;DB_CLOSE_DELAY=-1");
            config.setDriverClassName("org.h2.Driver");
            config.setUsername("sa");
            config.setPassword("");
        }

        String username = System.getenv("SPRING_DATASOURCE_USERNAME");
        String password = System.getenv("SPRING_DATASOURCE_PASSWORD");

        if (username != null) config.setUsername(username);
        if (password != null) config.setPassword(password);

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }
}
