package cn.juns.summer.db;

import cn.juns.summer.db.callback.ConnectionCallback;
import cn.juns.summer.db.callback.ResultSetMapper;
import cn.juns.summer.db.callback.SqlExecuteCallback;
import cn.juns.summer.db.callback.SqlQueryCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStorage {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStorage.class);
    @Autowired
    private StorageManager storageManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    protected AbstractStorage() {
    }

    protected boolean execute(Sql sql) {
        return this.execute(sql, new SqlExecuteCallback<Boolean>() {
            @Override
            public Boolean execute(Connection conn, Sql sql) throws SQLException {
                PreparedStatement pre = sql.createPreparedStatement(conn);
                return pre.execute();
            }
        });
    }

    protected int[] executeBatch(Sql sql) {
        return this.execute(sql, new SqlExecuteCallback<int[]>() {
            @Override
            public int[] execute(Connection conn, Sql sql) throws SQLException {
                PreparedStatement pre = null;
                int[] result;
                try {
                    sql.createPreparedStatement(conn);
                    result = pre.executeBatch();
                } finally {
                    JdbcUtils.closeStatement(pre);
                }
                return result;
            }
        });
    }

    protected int executeUpdate(Sql sql) {
        return this.execute(sql, new SqlExecuteCallback<Integer>() {
            @Override
            public Integer execute(Connection conn, Sql sql) throws SQLException {
                PreparedStatement pre = null;
                int result;
                try {
                    sql.createPreparedStatement(conn);
                    result = pre.executeUpdate();
                } finally {
                    JdbcUtils.closeStatement(pre);
                }
                return result;
            }
        });
    }

    protected <T> List<T> executeQuery(final Sql sql, final ResultSetMapper<T> mapper) {
        return this.executeQuery(sql, new SqlQueryCallback<List<T>>() {
            @Override
            public List<T> execute(ResultSet rs) throws SQLException {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapper.mapper(rs));
                }
                return list;
            }
        });
    }

    protected final <T> T executeQuery(final Sql sql, final SqlQueryCallback<T> callback) {
        return this.execute(sql, new SqlExecuteCallback<T>() {
            @Override
            public T execute(Connection conn, Sql sql) throws SQLException {
                PreparedStatement pre = null;
                ResultSet rs = null;
                T obj;
                try {
                    pre = sql.createPreparedStatement(conn);
                    rs = pre.executeQuery();
                    // 通过回调转化为相应对象
                    obj = callback.execute(rs);
                } finally {
                    JdbcUtils.closeResultSet(rs);
                    JdbcUtils.closeStatement(pre);
                }
                return obj;
            }
        });
    }

    protected final <T> T execute(final Sql sql, final SqlExecuteCallback<T> callback) {
        // 调用本类的最终执行方法
        return this.execute(new ConnectionCallback<T>() {
            @Override
            public T execute(Connection conn, ExecuteWatcher watcher) throws SQLException {
                watcher.setSql(sql.toString());
                // 又委托给了SqlExecuteCallback
                return callback.execute(conn, sql);
            }
        });
    }

    // 最终执行方法
    protected final <T> T execute(final ConnectionCallback<T> callback) {
        long from = System.currentTimeMillis();
        final StorageManager.SqlLine sqlLine = new StorageManager.SqlLine();
        boolean hasError = false;
        T t = null;

        try {
            final ConnectionCallback.ExecuteWatcher watcher = new ConnectionCallback.ExecuteWatcher() {
                public void setSql(String sql) {
                    sqlLine.sql = sql;
                    AbstractStorage.LOG.debug(sql);
                }
            };
            boolean available = this.storageManager.isAvailable();
            if (available) {
                t = this.jdbcTemplate.execute(new org.springframework.jdbc.core.ConnectionCallback<T>() {
                    @Override
                    public T doInConnection(Connection connection) throws SQLException, DataAccessException {
                        // 最终执行交给我们自己的实现类
                        return callback.execute(connection, watcher);
                    }
                });
            }
        } catch (Throwable throwable) {
            LOG.error(sqlLine.sql, throwable);
            hasError = true;
            sqlLine.errorInfo = throwable.getMessage();
            throw new RuntimeException(throwable);
        } finally {
            sqlLine.usedTime = System.currentTimeMillis() - from;
            if (hasError) {
                this.storageManager.addErrorCount(sqlLine);
            } else {
                this.storageManager.addExecuteCount(sqlLine);
            }
        }
        return t;
    }

    // 方法有歧义
    protected long countBySql(Sql sql) {
        return this.executeQuery(sql, new SqlQueryCallback<Long>() {
            @Override
            public Long execute(ResultSet rs) throws SQLException {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        });
    }




    protected void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException var3) {
                LOG.warn((String)null, var3);
            }
        }

    }

    protected void close(Statement sta) {
        try {
            if (sta != null) {
                sta.close();
            }
        } catch (SQLException var3) {
            LOG.warn((String)null, var3);
        }

    }

    protected void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException var3) {
            LOG.warn((String)null, var3);
        }

    }

    protected Connection getConnection() throws SQLException {
        Connection conn = this.jdbcTemplate.getDataSource().getConnection();
        if (conn == null) {
            LOG.warn("数据库连接为null");
        } else {
            LOG.debug("获取数据库连接:" + conn.getMetaData().getURL());
        }
        return conn;
    }
}
