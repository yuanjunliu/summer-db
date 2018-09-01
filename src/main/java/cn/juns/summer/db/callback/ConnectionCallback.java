package cn.juns.summer.db.callback;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionCallback<T> {
    T execute(Connection conn, ExecuteWatcher watcher) throws SQLException;

    public interface ExecuteWatcher {
        void setSql(String sql);
    }
}
