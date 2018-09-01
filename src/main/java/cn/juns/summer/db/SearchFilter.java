package cn.juns.summer.db;

import cn.juns.summer.db.dao.EntityField;

public class SearchFilter {
    public String fieldName;
    public Operator operator;
    public Object value;

    private SearchFilter(String fieldName, Operator operator, Object value) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
    }

    public SearchFilter(EntityField ef, Operator operator, Object value) {
        this(ef.getFieldName(), operator, value);
    }

    public static enum Operator {
        EQ,
        NEQ,
        LIKE,
        NLIKE,
        CUSTOM_LIKE,
        GT,
        LT,
        GTE,
        LTE,
        ISNULL,
        IS_NOT_NULL,
        STARTING,
        ENDING,
        LENGTH_EQ,
        LENGTH_NEQ,
        IN,
        NOT_IN,
        BIT_AND;

        private Operator() {
        }
    }
}
