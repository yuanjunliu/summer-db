package cn.juns.summer.db.callback;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetMapper<T> {
    T mapper(ResultSet set) throws SQLException;
}
