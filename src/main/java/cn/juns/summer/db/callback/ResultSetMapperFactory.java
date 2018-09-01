package cn.juns.summer.db.callback;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResultSetMapperFactory {
    public ResultSetMapperFactory() {
    }

    public static <T> ResultSetMapper<T> createBeanMapper(Class<T> cls) {
        if (cls == String.class) {
            return new ResultSetMapper<T>() {
                public T mapper(ResultSet rs) throws SQLException {
                    return (T)rs.getString(1);
                }
            };
        } else if (cls != Integer.class && cls != Integer.TYPE) {
            if (cls != Long.class && cls != Long.TYPE) {
                if (cls != Float.class && cls != Float.TYPE) {
                    if (cls != Double.class && cls != Double.TYPE) {
                        if (cls != Boolean.class && cls != Boolean.TYPE) {
                            if (Date.class.isAssignableFrom(cls)) {
                                return new ResultSetMapper<T>() {
                                    public T mapper(ResultSet rs) throws SQLException {
                                        return (T)rs.getTimestamp(1);
                                    }
                                };
                            } else {
                                return (ResultSetMapper)(Map.class.isAssignableFrom(cls) ? new ResultSetMapper<T>() {
                                    public T mapper(ResultSet rs) throws SQLException {
                                        ResultSetMetaData metaData = rs.getMetaData();
                                        Map<String, Object> map = new LinkedHashMap();
                                        int columnCount = metaData.getColumnCount();

                                        for(int i = 0; i < columnCount; ++i) {
                                            String columnName = metaData.getColumnLabel(i + 1);
                                            Object value = rs.getObject(i + 1);
                                            map.put(columnName, value);
                                        }

                                        return (T)map;
                                    }
                                } : ResultSetMapperFactory.BeanResultSetMapper.newInstance(cls));
                            }
                        } else {
                            return new ResultSetMapper<T>() {
                                public T mapper(ResultSet rs) throws SQLException {
                                    return (T)Boolean.valueOf(rs.getBoolean(1));
                                }
                            };
                        }
                    } else {
                        return new ResultSetMapper<T>() {
                            public T mapper(ResultSet rs) throws SQLException {
                                return (T)Double.valueOf(rs.getDouble(1));
                            }
                        };
                    }
                } else {
                    return new ResultSetMapper<T>() {
                        public T mapper(ResultSet rs) throws SQLException {
                            return (T)Float.valueOf(rs.getFloat(1));
                        }
                    };
                }
            } else {
                return new ResultSetMapper<T>() {
                    public T mapper(ResultSet rs) throws SQLException {
                        return (T)Long.valueOf(rs.getLong(1));
                    }
                };
            }
        } else {
            return new ResultSetMapper<T>() {
                public T mapper(ResultSet rs) throws SQLException {
                    return (T)Integer.valueOf(rs.getInt(1));
                }
            };
        }
    }

    public static ResultSetMapper<Map<String, Object>> createMapMapper() {
        return new ResultSetMapper<Map<String, Object>>() {
            public Map<String, Object> mapper(ResultSet rs) throws SQLException {
                ResultSetMetaData metaData = rs.getMetaData();
                Map<String, Object> map = new HashMap();
                int columnCount = metaData.getColumnCount();

                for(int i = 0; i < columnCount; ++i) {
                    String columnName = metaData.getColumnLabel(i + 1);
                    Object value = rs.getObject(i + 1);
                    map.put(columnName, value);
                }

                return map;
            }
        };
    }

    private static class BeanResultSetMapper<T> extends BeanPropertyRowMapper<T> implements ResultSetMapper<T> {
        private int rowNumber = 0;

        private BeanResultSetMapper() {
        }

        public T mapper(ResultSet rs) throws SQLException {
            return this.mapRow(rs, this.rowNumber++);
        }

        public static <T> ResultSetMapperFactory.BeanResultSetMapper<T> newInstance(Class<T> mappedClass) {
            ResultSetMapperFactory.BeanResultSetMapper<T> newInstance = new ResultSetMapperFactory.BeanResultSetMapper();
            newInstance.setMappedClass(mappedClass);
            return newInstance;
        }
    }
}
