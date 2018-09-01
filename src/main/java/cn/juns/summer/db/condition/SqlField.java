package cn.juns.summer.db.condition;

import cn.juns.summer.db.dao.EntityField;
import org.apache.commons.lang3.StringUtils;

public class SqlField {
    private EntityField field;
    private SqlFunc sqlFunc;
    private String alias;

    public SqlField() {
    }

    public SqlField(EntityField field) {
        this.field = field;
    }

    public SqlField(EntityField field, SqlFunc sqlFunc) {
        this.field = field;
        this.sqlFunc = sqlFunc;
    }

    public SqlField(EntityField field, SqlFunc sqlFunc, String alias) {
        this.field = field;
        this.sqlFunc = sqlFunc;
        this.alias = alias;
    }

    public String toSqlString() {
        String str = null;
        if (sqlFunc != null) {
            str = sqlFunc.convert(field);
        } else {
            str = "`" + field.getColumnName() + "`";
        }
        return StringUtils.isNotBlank(alias) ? str + " as " + alias : str;
    }
}
