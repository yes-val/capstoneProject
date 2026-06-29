package kz.epam.campus.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class ConnectionPool implements DataSource {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int minIdle;
    private final int maxPoolSize;
    private final long connectionTimeoutMillis;

    private final BlockingQueue<Connection> idleConnections;
    private final AtomicInteger totalConnections = new AtomicInteger(0);

    public ConnectionPool(String jdbcUrl, String username, String password,
                          int minIdle, int maxPoolSize, long connectionTimeoutMillis) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.minIdle = minIdle;
        this.maxPoolSize = maxPoolSize;
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        this.idleConnections = new LinkedBlockingQueue<>(maxPoolSize);

        for (int i = 0; i < minIdle; i++) {
            idleConnections.offer(createRawConnection());
            totalConnections.incrementAndGet();
        }
    }

    private Connection createRawConnection() {
        try {
            Class.forName("org.h2.Driver");
            return java.sql.DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create pooled connection", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not found on classpath", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection raw = idleConnections.poll();

        if (raw == null) {
            if (totalConnections.get() < maxPoolSize) {
                raw = createRawConnection();
                totalConnections.incrementAndGet();
            } else {
                try {
                    raw = idleConnections.poll(connectionTimeoutMillis, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for a connection", e);
                }
                if (raw == null) {
                    throw new SQLException("Timed out waiting for a connection from the pool");
                }
            }
        }

        return new PooledConnection(raw, this);
    }

    void releaseConnection(Connection raw) {
        idleConnections.offer(raw);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException("unwrap not supported");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger not supported");
    }
}
