package cn.juns.summer.db.callback;

import cn.juns.summer.db.Sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlExecuteCallback<T> {
    T execute(Connection conn, Sql sql) throws SQLException;
}
