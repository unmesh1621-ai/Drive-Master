package com.drivemaster.drivemastermain.config;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import com.drivemaster.drivemastermain.dao.tx.TransactionManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class DataSourceConfig {

    @Bean(destroyMethod = "shutdown")
    public ConnectionPool connectionPool(AppProperties props) {
        AppProperties.Datasource ds = props.getDatasource();
        AppProperties.Pool pool = ds.getPool();
        return new ConnectionPool(
                ds.getUrl(),
                ds.getUsername(),
                ds.getPassword(),
                pool.getMinSize(),
                pool.getMaxSize(),
                pool.getConnectionTimeoutMs());
    }

    @Bean
    public TransactionManager transactionManager(ConnectionPool connectionPool) {
        return new TransactionManager(connectionPool);
    }
}
