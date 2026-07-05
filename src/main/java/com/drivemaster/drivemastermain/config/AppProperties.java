package com.drivemaster.drivemastermain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Datasource datasource = new Datasource();
    private Jwt jwt = new Jwt();
    private Notification notification = new Notification();
    private Admin admin = new Admin();

    public Datasource getDatasource() { return datasource; }
    public void setDatasource(Datasource datasource) { this.datasource = datasource; }

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }

    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }

    public static class Datasource {
        private String url;
        private String username;
        private String password;
        private boolean initSchema = true;
        @NestedConfigurationProperty
        private Pool pool = new Pool();

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public boolean isInitSchema() { return initSchema; }
        public void setInitSchema(boolean initSchema) { this.initSchema = initSchema; }

        public Pool getPool() { return pool; }
        public void setPool(Pool pool) { this.pool = pool; }
    }

    public static class Pool {
        private int minSize = 5;
        private int maxSize = 20;
        private long connectionTimeoutMs = 5000;

        public int getMinSize() { return minSize; }
        public void setMinSize(int minSize) { this.minSize = minSize; }

        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }

        public long getConnectionTimeoutMs() { return connectionTimeoutMs; }
        public void setConnectionTimeoutMs(long connectionTimeoutMs) { this.connectionTimeoutMs = connectionTimeoutMs; }
    }

    public static class Jwt {
        private String secret;
        private long expirationMs = 3_600_000L;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    }

    public static class Notification {
        private int poolSize = 4;

        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
    }

    public static class Admin {
        private String bootstrapEmail = "admin@drivemaster.local";
        private String bootstrapPassword = "ChangeMe_123!";

        public String getBootstrapEmail() { return bootstrapEmail; }
        public void setBootstrapEmail(String bootstrapEmail) { this.bootstrapEmail = bootstrapEmail; }

        public String getBootstrapPassword() { return bootstrapPassword; }
        public void setBootstrapPassword(String bootstrapPassword) { this.bootstrapPassword = bootstrapPassword; }
    }
}
