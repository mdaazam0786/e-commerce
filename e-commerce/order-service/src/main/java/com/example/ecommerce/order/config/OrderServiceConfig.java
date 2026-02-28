package com.example.ecommerce.order.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
    basePackages = "com.example.ecommerce.order.repository"
)
@EnableTransactionManagement
public class OrderServiceConfig {
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:100}")
    private int maxPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:25}")
    private int minIdle;
    
    @Value("${spring.datasource.hikari.idle-timeout:60000}")
    private int idleTimeout;
    
    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private int connectionTimeout;
    
    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private int maxLifeTime;
    
    @Value("${spring.datasource.hikari.leak-detection-threshold:30000}")
    private int leakDetectionThreshold;
    
    @Value("${spring.datasource.hikari.validation-timeout:5000}")
    private int validationTimeout;
    
    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;
    
    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;
    
    @Bean
    public DataSource orderEntityDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setDriverClassName(driverClassName);
        
        // Pool Configuration
        hikariConfig.setPoolName("OrderServicePool");
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(minIdle);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setMaxLifetime(maxLifeTime);
        hikariConfig.setAutoCommit(true);
        
        // Additional Safety Configurations
        hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        hikariConfig.setValidationTimeout(validationTimeout);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        
        return new HikariDataSource(hikariConfig);
    }
    
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean orderManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(showSql);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        
        HibernateJpaDialect jpd = new HibernateJpaDialect();
        
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaDialect(jpd);
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.example.ecommerce.order.model");
        factory.setDataSource(orderEntityDataSource());
        
        return factory;
    }
    
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
