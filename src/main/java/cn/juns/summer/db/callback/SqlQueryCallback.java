package cn.juns.summer.db.callback;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SqlQueryCallback<T> {
    T execute(ResultSet resultSet) throws SQLException;
}
