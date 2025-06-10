package ma.ensa.ebankingver1.config;


import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("ma.ensa.ebankingver1.repository")
@PropertySource("classpath:database.properties")
public class DataConfig {

    @Autowired
    private Environment environment;

    private final String PROPERTY_DRIVER = "driver";
    private final String PROPERTY_URL = "url";
    private final String PROPERTY_USERNAME = "user";
    private final String PROPERTY_PASSWORD = "password";
    private final String PROPERTY_SHOW_SQL = "hibernate.show_sql";
    private final String PROPERTY_HBM2DDL = "hibernate.hbm2ddl.auto";
    private final String PROPERTY_DIALECT = "hibernate.dialect";
    private final String PROPERTY_FORMAT_SQL = "hibernate.format_sql";
    private final String PROPERTY_USE_SQL_COMMENTS = "hibernate.use_sql_comments";
    private final String PROPERTY_CACHE_SECOND_LEVEL = "hibernate.cache.use_second_level_cache";
    private final String PROPERTY_CACHE_QUERY = "hibernate.cache.use_query_cache";
    private final String PROPERTY_ID_GENERATOR = "hibernate.id.new_generator_mappings";


    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lfb = new LocalContainerEntityManagerFactoryBean();
        lfb.setDataSource(dataSource());
        lfb.setPackagesToScan("ma.ensa.ebankingver1.model");
        lfb.setJpaProperties(hibernateProps());
        lfb.setPersistenceProviderClass(org.hibernate.jpa.HibernatePersistenceProvider.class); // Fournisseur explicite
        return lfb;
    }
    @Bean
    DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(environment.getProperty(PROPERTY_URL));
        ds.setUsername(environment.getProperty(PROPERTY_USERNAME));
        ds.setPassword(environment.getProperty(PROPERTY_PASSWORD));
        ds.setDriverClassName(environment.getProperty(PROPERTY_DRIVER));
        return ds;
    }

    Properties hibernateProps() {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_SHOW_SQL, environment.getProperty(PROPERTY_SHOW_SQL, "true"));
        properties.setProperty(PROPERTY_FORMAT_SQL, environment.getProperty(PROPERTY_FORMAT_SQL, "true"));
        properties.setProperty(PROPERTY_USE_SQL_COMMENTS, environment.getProperty(PROPERTY_USE_SQL_COMMENTS, "true"));
        properties.setProperty(PROPERTY_HBM2DDL, environment.getProperty(PROPERTY_HBM2DDL, "none"));
        properties.setProperty(PROPERTY_DIALECT, environment.getProperty(PROPERTY_DIALECT, "org.hibernate.dialect.PostgreSQLDialect"));
        properties.setProperty(PROPERTY_CACHE_SECOND_LEVEL, environment.getProperty(PROPERTY_CACHE_SECOND_LEVEL, "false"));
        properties.setProperty(PROPERTY_CACHE_QUERY, environment.getProperty(PROPERTY_CACHE_QUERY, "false"));
        properties.setProperty(PROPERTY_ID_GENERATOR, environment.getProperty(PROPERTY_ID_GENERATOR, "false"));
        return properties;
    }

    @Bean
    JpaTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @PreDestroy
    public void destroy() {
        // Cleanup resources if needed
    }
}
