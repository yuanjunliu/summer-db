package cn.juns.summer.db.dao;

import java.util.Objects;

public class EntityField {
    private final String fieldName;
    private final String columnName;
    private final Class<?> fieldClass;
    private final boolean nullable;
    private final int columnSize;

    public EntityField(String fieldName, String columnName, Class<?> fieldClass, boolean nullable, int columnSize) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.fieldClass = fieldClass;
        this.nullable = nullable;
        this.columnSize = columnSize;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    public boolean isNullable() {
        return nullable;
    }

    public int getColumnSize() {
        return columnSize;
    }



    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.fieldName);
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            EntityField other = (EntityField)obj;
            return Objects.equals(this.fieldName, other.fieldName);
        }
    }
}
