package com.example.ecommerce.user.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
    basePackages = "com.example.ecommerce.user.repository"
)
public class UserServiceConfig {
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String datasourceUsername;
    
    @Value("${spring.datasource.password}")
    private String datasourcePassword;
    
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
    
    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(datasourceUrl);
        hikariConfig.setUsername(datasourceUsername);
        hikariConfig.setPassword(datasourcePassword);
        hikariConfig.setDriverClassName(driverClassName);
        
        hikariConfig.setPoolName("UserServicePool");
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(minIdle);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setMaxLifetime(maxLifeTime);
        hikariConfig.setAutoCommit(true);
        
        hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        hikariConfig.setValidationTimeout(validationTimeout);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        
        return new HikariDataSource(hikariConfig);
    }
    
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(false);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
        
        HibernateJpaDialect jpaDialect = new HibernateJpaDialect();
        
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaDialect(jpaDialect);
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.example.ecommerce.user.model");
        factory.setDataSource(dataSource());
        factory.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.format_sql", "true");
        factory.setJpaProperties(jpaProperties);
        
        return factory;
    }
    
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
