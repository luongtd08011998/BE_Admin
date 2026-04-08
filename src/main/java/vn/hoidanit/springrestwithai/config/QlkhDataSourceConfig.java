package vn.hoidanit.springrestwithai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "vn.hoidanit.springrestwithai.qlkh",
        entityManagerFactoryRef = "qlkhEntityManagerFactory",
        transactionManagerRef = "qlkhTransactionManager"
)
public class QlkhDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.qlkh")
    public DataSource qlkhDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean qlkhEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(qlkhDataSource());
        factory.setPackagesToScan("vn.hoidanit.springrestwithai.qlkh");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        factory.setJpaPropertyMap(props);
        return factory;
    }

    @Bean
    public PlatformTransactionManager qlkhTransactionManager() {
        return new JpaTransactionManager(qlkhEntityManagerFactory().getObject());
    }
}
