package cn.juns.summer.db.condition;

import cn.juns.summer.db.SearchFilter;
import cn.juns.summer.db.dao.EntityField;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Node {
    private EntityField field;
    private SearchFilter.Operator operator;
    private Object value;

    public Node() {}

    public static Node create(EntityField ef, SearchFilter.Operator operator, Object value) {
        Node node = new Node();
        node.setField(ef);
        node.setOperator(operator);
        node.setValue(value);
        return node;
    }

    public void toSqlString(String alias, StringBuilder sb, List<Object> params) {
        String columnName = "`" + this.field.getColumnName() + "`";
        if (StringUtils.isNotBlank(alias)) {
            columnName = alias + "." + columnName;
        }
        switch (this.getOperator()) {
            case EQ:
                sb.append(columnName).append(" =?");
                params.add(this.getValue());
                break;
            case NEQ:
                sb.append(columnName).append(" <>?");
                params.add(this.getValue());
                break;
            case LIKE:
                sb.append(columnName).append(" like ?");
                params.add("%" + this.getValue() + "%");
                break;
            case CUSTOM_LIKE:
                sb.append(columnName).append(" like ?");
                params.add(this.getValue());
                break;
            case NLIKE:
                sb.append(columnName).append(" not like ?");
                params.add("%" + this.getValue() + "%");
                break;
            case GT:
                sb.append(columnName).append(" >?");
                params.add(this.getValue());
                break;
            case LT:
                sb.append(columnName).append(" <?");
                params.add(this.getValue());
                break;
            case GTE:
                sb.append(columnName).append(" >=?");
                params.add(this.getValue());
                break;
            case LTE:
                sb.append(columnName).append(" <=?");
                params.add(this.getValue());
                break;
            case ISNULL:
                sb.append(columnName).append(" is null");
                break;
            case IS_NOT_NULL:
                sb.append(columnName).append(" is not null");
                break;
            case STARTING:
                sb.append(columnName).append(" like ?");
                params.add(this.getValue() + "%");
                break;
            case ENDING:
                sb.append(columnName).append(" like ?");
                params.add("%" + this.getValue());
                break;
            case LENGTH_EQ:
                sb.append(" length(").append(columnName).append(")=?");
                params.add(this.getValue());
                break;
            case LENGTH_NEQ:
                sb.append(" length(").append(columnName).append(")<>?");
                params.add(this.getValue());
                break;
            case BIT_AND:
                sb.append(columnName).append(" &?=?");
                params.add(this.getValue());
                params.add(this.getValue());
                break;
            case IN:
            case NOT_IN:
                if (this.getValue() == null) {
                    throw new RuntimeException("in 里面不能为空");
                }
                String[] ss;
                if (this.value instanceof Collection) {
                    Collection<Object> c = (Collection) this.value;
                    ss = c.stream().map(String::valueOf).toArray(String[]::new);
                } else if (this.value.getClass().isArray()) {
                    Object[] objs = (Object[]) this.value;
                    ss = Arrays.stream(objs).map(String::valueOf).toArray(String[]::new);
                } else {
                    ss = String.valueOf(this.value).split(",");
                }
                if (ss.length == 0) {
                    throw new RuntimeException("in 里面至少要有一个值");
                }
                sb.append(columnName);
                if (this.getOperator() == SearchFilter.Operator.NOT_IN) {
                    sb.append(" not in (");
                } else {
                    sb.append(" in (");
                }
                for(int index = 0; index < ss.length; ++index) {
                    if (index > 0) {
                        sb.append(" , ");
                    }

                    sb.append("?");
                    params.add(ss[index]);
                }

                sb.append(")");
        }
    }

    public EntityField getField() {
        return field;
    }

    public void setField(EntityField field) {
        this.field = field;
    }

    public SearchFilter.Operator getOperator() {
        return operator;
    }

    public void setOperator(SearchFilter.Operator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
