package ma.ensa.ebankingver1.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("ma.ensa.ebankingver1.repository")
@PropertySource("classpath:database.properties")
public class DataConfig {

    @Autowired
    private Environment environment;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lfb = new LocalContainerEntityManagerFactoryBean();
        lfb.setDataSource(dataSource());
        lfb.setPackagesToScan("ma.ensa.ebankingver1.model");
        lfb.setJpaProperties(hibernateProps());
        lfb.setPersistenceProviderClass(org.hibernate.jpa.HibernatePersistenceProvider.class);
        return lfb;
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(environment.getProperty("url"));
        ds.setUsername(environment.getProperty("user"));
        ds.setPassword(environment.getProperty("password"));
        ds.setDriverClassName(environment.getProperty("driver"));
        ds.setMaximumPoolSize(Integer.parseInt(environment.getProperty("hikari.maximumPoolSize", "10")));
        ds.setMinimumIdle(Integer.parseInt(environment.getProperty("hikari.minimumIdle", "5")));
        return ds;
    }

    @Bean
    public Properties hibernateProps() {
        Properties properties = new Properties();
        for (var key : ((org.springframework.core.env.AbstractEnvironment) environment).getPropertySources()) {
            if (key instanceof org.springframework.core.env.EnumerablePropertySource) {
                for (String name : ((org.springframework.core.env.EnumerablePropertySource<?>) key).getPropertyNames()) {
                    if (name.startsWith("hibernate.")) {
                        properties.setProperty(name, environment.getProperty(name));
                    }
                }
            }
        }
        return properties;
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @PreDestroy
    public void closeDataSource() {
        try {
            ((HikariDataSource) dataSource()).close();
            System.out.println("✅ HikariDataSource closed");
        } catch (Exception e) {
            System.err.println("❌ Failed to close HikariDataSource: " + e.getMessage());
        }
    }
}