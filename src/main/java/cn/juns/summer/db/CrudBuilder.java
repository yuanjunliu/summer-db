package cn.juns.summer.db;

import cn.juns.summer.db.annotation.*;
import cn.juns.summer.db.condition.Order;
import cn.juns.summer.db.condition.SqlField;
import cn.juns.summer.db.condition.SqlPath;
import cn.juns.summer.db.dao.ContentValues;
import cn.juns.summer.db.dao.EntityField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.*;

public class CrudBuilder<T> {
    private static final Logger LOG = LoggerFactory.getLogger(CrudBuilder.class);
    private final Class<T> clazz;
    private final Audit audit;
    private List<Field> fieldList;
    private Map<String, EntityField> entityFieldMapping;
    private Field idField;
    private String idName;
    private Sql insert;
    private Sql update;
    private Sql selectById;
    private Sql selectAll;
    private Sql delete;
    private String[] noAuditFields;
    private String[] titleFields;

    public CrudBuilder(Class<T> clazz) {
        this.clazz = clazz;
        this.audit = (Audit) clazz.getAnnotation(Audit.class);
        initFields();
        initEntityFieldMapping();
        initInsertSql();
        initSelectSql();
        initUpdateSql();
        initDeleteSql();
    }

    private void initFields() {
        this.fieldList = new ArrayList<>();
        List<String> noAuditFieldList = new ArrayList<>();
        List<Pair<String, Integer>> titleList = new ArrayList<>();
        for (Class cls = this.clazz; cls != Object.class; cls = cls.getSuperclass()) {
            Field[] fs = cls.getDeclaredFields();
            Field[] temp = fs;
            int len = fs.length;
            for (int i = 0; i < len; i++) {
                Field f = temp[i];
                Transient t = (Transient) f.getAnnotation(Transient.class);
                int mf = f.getModifiers();
                if (t == null && !Modifier.isFinal(mf) && !Modifier.isStatic(mf) && !Modifier.isTransient(mf)) {
                    this.fieldList.add(f);
                    f.setAccessible(true);
                    if (this.audit != null) {
                        NoAudit noAudit = (NoAudit)f.getAnnotation(NoAudit.class);
                        if (noAudit != null) {
                            noAuditFieldList.add(f.getName());
                        }

                        Title title = (Title)f.getAnnotation(Title.class);
                        if (title != null) {
                            Pair<String, Integer> pair = new ImmutablePair(f.getName(), title.priority());
                            titleList.add(pair);
                        }
                    }
                    Id id = (Id) f.getAnnotation(Id.class);
                    if (id != null) {
                        this.idField = f;
                        this.idName = id.name();
                    }
                }
            }
        }
        if (StringUtils.isBlank(this.idName)) {
            Iterator var14 = this.fieldList.iterator();

            while(var14.hasNext()) {
                Field f = (Field)var14.next();
                if (StringUtils.equalsIgnoreCase("id", f.getName())) {
                    this.idField = f;
                    Column column = (Column)f.getAnnotation(Column.class);
                    if (column != null) {
                        this.idName = column.name();
                    } else {
                        this.idName = "id";
                    }

                    LOG.warn("当前实体类:{}没有定义主键注解，将使用:{} 做为主键名", this.clazz, this.idName);
                }
            }
        }

        if (StringUtils.isBlank(this.idName)) {
            throw new IllegalArgumentException("当前实体类:{} 找不到主键");
        } else if (this.idField.getType() != Long.class) {
            throw new IllegalArgumentException("当前实体类:{} 主键类型不是Long，而是:" + this.idField.getType());
        }
    }

    private void initEntityFieldMapping() {
        this.entityFieldMapping = new HashMap<>();
        for (Field field : fieldList) {
            if (field != this.idField) {
                String fieldName = field.getName();
                EntityField ef = getEntityField(fieldName);
                if (ef != null) {
                    this.entityFieldMapping.put(fieldName, ef);
                }
            }
        }
    }

    private EntityField getEntityField(String fieldName) {
        try {
            Field field = this.clazz.getDeclaredField("Fields");
            Object fields = field.get((Object)null);
            Field f = field.getClass().getField(fieldName);
            return (EntityField) f.get(fields);
        } catch (Exception e) {
            LOG.debug("No field named 'fields' found, try to find inner class");
        }
        Class<?>[] classes = this.clazz.getDeclaredClasses();
        for (Class<?> c : classes) {
            if ("Fields".equals(c.getSimpleName())) {
                try {
                    Field f = c.getField(fieldName);
                    return (EntityField) f.get((Object) null);
                } catch (Exception e) {
                    LOG.debug(null, e);
                }
            }
        }
        LOG.warn("can not find {}'s EntityField", fieldName);
        return null;
    }

    private void initInsertSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(this.getTableName());
        sb.append(" (").append(getFieldListString()).append(")");
        sb.append(" values (");
        for (int i = 0; i < this.fieldList.size(); i++) {
            Field f = this.fieldList.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }

        sb.append(")");
        this.insert = new Sql(sb.toString());
    }

    private void initUpdateSql() {
        StringBuilder set = new StringBuilder();
        for (int i = 0; i < this.fieldList.size(); i++) {
            Field f = this.fieldList.get(i);
            if (f != this.idField) {
                if (i > 0) {
                    set.append(",");
                }
                set.append("`").append(getColumn(fieldList.get(i))).append("`=?");
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("update table ").append(this.getTableName());
        sb.append(" set ").append(set.toString());
        sb.append(" where ").append(idName).append("=?");
        this.update = new Sql(sb.toString());
    }

    private void initSelectSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(getFieldListString());
        sb.append(" from ").append(getTableName());
        this.selectAll = new Sql(sb.toString());

        sb.append(" where ").append(idName).append("=?");
        this.selectById = new Sql(sb.toString());
    }

    private void initDeleteSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("delete from ").append(this.getTableName());
        sb.append(" where ").append(this.idName).append(" =? ");
        this.delete = new Sql(sb.toString());
    }

    public String getTableName() {
        try {
            Table table = this.clazz.getAnnotation(Table.class);
            if (table != null) {
                return table.name();
            }
        } catch (Exception e) {
            LOG.error((String)null, e);
        }
        return convertToTableName(this.clazz.getSimpleName());
    }

    public static String convertToTableName(String clazzName) {
        if (clazzName.endsWith("Entity")) {
            clazzName = clazzName.substring(0, clazzName.length() - 6);
        }
        return "t" + convertToColumnName(clazzName);
    }

    public static String convertToColumnName(String fieldName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String getFieldListString() {
        StringBuilder sb = new StringBuilder();
        for (Field field : fieldList) {
            sb.append("`").append(getColumn(field)).append("`,");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public String getColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column != null ? column.name() : convertToColumnName(field.getName());
    }

    public Sql buildUpdateSql(ContentValues cv) {
        StringBuilder set = new StringBuilder();
        List<Object> params = new ArrayList<>();
        int count = 0;
        final String idNameLocal = this.idName;
        Object idValue = null;
        for (Map.Entry<String, Object> entry : cv.toColumnsMap().entrySet()) {
            if (StringUtils.equals(entry.getKey(), idNameLocal)) {
                idValue = entry.getValue();
            } else {
                if (count > 0) {
                    set.append(",");
                }
                set.append("`").append(entry.getKey()).append("`=?");
                params.add(entry.getValue());
            }
            count++;
        }
        if (idValue == null) {
            throw new IllegalArgumentException("could not find primary key");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("update table ").append(this.getTableName());
            sb.append(" set ").append(set);
            sb.append(" where ").append(idNameLocal).append("=?");
            Sql sql = new Sql(sb.toString());
            sql.addAllParams(params);
            sql.addParam(idValue);
            return sql;
        }
    }

    public Sql buildInsertSql(ContentValues cv) {
        StringBuilder into = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Object> entry : cv.toColumnsMap().entrySet()) {
            if (count > 0) {
                into.append(",");
                values.append(",");
            }
            into.append("`").append(entry.getKey()).append("`");
            values.append("?");
            params.add(entry.getValue());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(this.getTableName());
        sb.append(" (").append(into).append(")");
        sb.append(" values(").append(values).append(")");
        Sql sql = new Sql(sb.toString());
        sql.addAllParams(params);
        return sql;
    }

    public Sql buildSelectSql(SqlPath sqlPath) {
        if (sqlPath == null) {
            return Sql.from("select * from " + getTableName());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            List<SqlField> fields = sqlPath.getSelectFields();
            if (CollectionUtils.isEmpty(fields)) {
                sb.append("*");
            } else {
                int count = 0;
                for (SqlField field : fields) {
                    if (count > 0) {
                        sb.append(",");
                    }
                    sb.append(field.toSqlString());
                    count++;
                }
            }
            sb.append(" from ").append(getTableName());
            final String alias = "";
            List<Object> params = new ArrayList<>(10);
            if (sqlPath.getNode() != null) {
                sb.append(" where ");
                sqlPath.getNode().toSqlString(alias, sb, params);
            }
            if (!CollectionUtils.isEmpty(sqlPath.getOrders())) {
                sb.append(" order by ");
                int count = 0;
                for (Order order : sqlPath.getOrders()) {
                    if (count > 0) {
                        sb.append(",");
                    }
                    if (StringUtils.isNotBlank(alias)) {
                        sb.append(alias).append(".");
                    }
                    sb.append(order.getFieldName());
                    sb.append(" ").append(order.getDirection());
                    count++;
                }
            }
            if (sqlPath.getPage() != null) {
                sb.append(" limit ");
                sb.append(sqlPath.getPage().getOffset()).append(",").append(sqlPath.getPage().getLimit());
            }
            return Sql.from(sb.toString(), params.toArray());
        }
    }

    public Sql buildSelectSql(List<SearchFilter> filters, PageRequest page) {
        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(getFieldListString());
        sb.append(" from ").append(getTableName());
        if (!CollectionUtils.isEmpty(filters)) {
            buildConditions(null, sb, filters, params);
        }
        if (page != null) {
            if (!CollectionUtils.isEmpty(page.getSort())) {
                sb.append(" order by");
                int count = 0;
                for (PageRequest.Sort sort : page.getSort()) {
                    if (count > 0) {
                        sb.append(",");
                    }
                    sb.append(" ").append(getColumnName(sort.getProperty()));
                    sb.append(" ").append(sort.getDirection().name());
                }
            }
            sb.append(" limit ").append(page.getOffSet()).append(",").append(page.getLimit());
        }
        Sql sql = new Sql(sb.toString());
        sql.addAllParams(params);
        return sql;
    }

    public void buildConditions(String alias, StringBuilder sb, List<SearchFilter> filters, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters)) {
            String temp = sb.toString().toLowerCase();
            if (!temp.contains("where")) {
                sb.append(" where ");
            } else {
                sb.append(" and ");
            }
            alias = StringUtils.isBlank(alias) ? "" : alias + ".";
            for (SearchFilter filter : filters) {
                sb.append(" and ");
                String columnName = alias + "`" + getColumnName(filter.fieldName) + "`";
                switch(filter.operator) {
                    case EQ:
                        sb.append(columnName).append(" =?");
                        params.add(filter.value);
                        break;
                    case NEQ:
                        sb.append(columnName).append(" <>?");
                        params.add(filter.value);
                        break;
                    case LIKE:
                        sb.append(columnName).append(" like ?");
                        params.add("%" + filter.value + "%");
                        break;
                    case CUSTOM_LIKE:
                        sb.append(columnName).append(" like ?");
                        params.add(filter.value.toString());
                        break;
                    case NLIKE:
                        sb.append(columnName).append(" not like ?");
                        params.add("%" + filter.value + "%");
                        break;
                    case GT:
                        sb.append(columnName).append(" >?");
                        params.add(filter.value);
                        break;
                    case LT:
                        sb.append(columnName).append(" <?");
                        params.add(filter.value);
                        break;
                    case GTE:
                        sb.append(columnName).append(" >=?");
                        params.add(filter.value);
                        break;
                    case LTE:
                        sb.append(columnName).append(" <=?");
                        params.add(filter.value);
                        break;
                    case ISNULL:
                        sb.append(columnName).append(" is null");
                        break;
                    case IS_NOT_NULL:
                        sb.append(columnName).append(" is not null");
                        break;
                    case STARTING:
                        sb.append(columnName).append(" like ?");
                        params.add(filter.value + "%");
                        break;
                    case ENDING:
                        sb.append(columnName).append(" like ?");
                        params.add("%" + filter.value);
                        break;
                    case LENGTH_EQ:
                        sb.append(" length(").append(columnName).append(")=?");
                        params.add(filter.value);
                        break;
                    case LENGTH_NEQ:
                        sb.append(" length(").append(columnName).append(")<>?");
                        params.add(filter.value);
                        break;
                    case BIT_AND:
                        sb.append(" ").append(columnName).append("&?=?");
                        params.add(filter.value);
                        params.add(filter.value);
                        break;
                    case IN:
                    case NOT_IN:
                        if (filter.value == null) {
                            throw new RuntimeException("in 条件里不可以为空");
                        }

                        String[] ss;
                        int index;
                        if (filter.value instanceof List) {
                            List<Object> lo = (List) filter.value;
                            ss = new String[lo.size()];

                            for (index = 0; index < lo.size(); ++index) {
                                ss[index] = String.valueOf(lo.get(index));
                            }
                        } else if (filter.value instanceof Set) {
                            Set<Object> so = (Set) filter.value;
                            ss = new String[so.size()];
                            index = 0;

                            Object v;
                            for (Iterator var12 = so.iterator(); var12.hasNext(); ss[index++] = String.valueOf(v)) {
                                v = var12.next();
                            }
                        } else if (filter.value.getClass().isArray()) {
                            Object[] os = (Object[]) ((Object[]) filter.value);
                            ss = new String[os.length];

                            for (index = 0; index < os.length; ++index) {
                                ss[index] = String.valueOf(os[index]);
                            }
                        } else {
                            ss = String.valueOf(filter.value).split(",");
                        }

                        if (ss.length <= 0) {
                            throw new RuntimeException("in条件里至少要有一个值");
                        }

                        if (filter.operator == SearchFilter.Operator.NOT_IN) {
                            sb.append(columnName).append(" not in (");
                        } else {
                            sb.append(columnName).append(" in (");
                        }

                        for (index = 0; index < ss.length; ++index) {
                            if (index > 0) {
                                sb.append(" , ");
                            }

                            sb.append("?");
                            params.add(ss[index]);
                        }

                        sb.append(")");
                }
            }
        }
    }

    public Sql buildCount(List<SearchFilter> filters) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ").append(getTableName());
        List<Object> params = new ArrayList<>();
        if (!CollectionUtils.isEmpty(filters)) {
            buildConditions("", sb, filters, params);
        }
        return Sql.from(sb.toString(), params);
    }

    public Sql buildCount(SqlPath sqlPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ").append(getTableName());
        List<Object> params = new ArrayList<>();
        if (sqlPath != null && sqlPath.getNode() != null) {
            sb.append(" where ");
            sqlPath.getNode().toSqlString("", sb, params);
        }
        return Sql.from(sb.toString(), params);
    }

    public List<Object> buildInsertParams(T t) {
        final List<Field> fieldListLocal = this.fieldList;
        List<Object> params = new ArrayList<>(fieldListLocal.size());
        for (Field f:fieldListLocal) {
            try {
                params.add(f.get(t));
            } catch (IllegalAccessException e) {
                LOG.error(null, e);
            }
        }
        return params;
    }

    public List<Object> buildUpdateParams(T t) {
        final List<Field> fieldListLocal = this.fieldList;
        List<Object> params = new ArrayList<>(fieldListLocal.size());
        for (Field f:fieldListLocal) {
            try {
                params.add(f.get(t));
            } catch (IllegalAccessException e) {
                LOG.error(null, e);
            }
        }
        try {
            params.add(idField.get(t));
        } catch (IllegalAccessException e) {
            LOG.error(null, e);
        }
        return params;
    }

    public String getColumnName(String fieldName) {
        Iterator var2 = this.fieldList.iterator();

        Field field;
        do {
            if (!var2.hasNext()) {
                return convertToColumnName(fieldName);
            }

            field = (Field)var2.next();
        } while(!field.getName().equals(fieldName));

        return getColumnName(field);
    }

    public static String getColumnName(Field field) {
        Column column = (Column)field.getAnnotation(Column.class);
        return column != null ? column.name() : convertToColumnName(field.getName());
    }

    public T from(ResultSet rs) throws SQLException{
        T t;
        try {
            t = this.clazz.newInstance();
        } catch (Exception e) {
            throw new SQLException(e);
        }
        if (t != null) {
            for (Field f : fieldList) {
                try {
                    setFieldIfNeed(f, rs, t);
                } catch (IllegalAccessException e) {
                    LOG.debug("not find value in ResultSet for: {}", f.getName());
                }
            }
        }
        return t;
    }

    private void setFieldIfNeed(Field f, ResultSet rs, T t)
        throws SQLException, IllegalAccessException{
        Class<?> type = f.getType();
        String columnName = getColumnName(f);
        if (type == String.class) {
            f.set(t, rs.getString(columnName));
        } else if (type == Integer.class) {
            f.setInt(t, rs.getInt(columnName));
        } else if (type == Long.class) {
            f.setLong(t, rs.getLong(columnName));
        } else if (type == Float.class) {
            f.setFloat(t, rs.getFloat(columnName));
        } else if (type == Double.class) {
            f.setDouble(t, rs.getDouble(columnName));
        } else if (type == Byte.class) {
            f.setByte(t, rs.getByte(columnName));
        } else if (type == Short.class) {
            f.setShort(t, rs.getShort(columnName));
        } else if (type == Boolean.class) {
            f.setBoolean(t, rs.getBoolean(columnName));
        } else if (type == Date.class) {
            f.set(t, rs.getDate(columnName));
        } else if (type == Time.class) {
            f.set(t, rs.getTime(columnName));
        } else if (java.util.Date.class.isAssignableFrom(type)) {
            f.set(t, rs.getTimestamp(columnName));
        } else {
            f.set(t, rs.getObject(columnName));
        }
    }

    public Sql getInsert() {
        return insert.clone();
    }

    public Sql getUpdate() {
        return update.clone();
    }

    public Sql getSelectById() {
        return selectById.clone();
    }

    public Sql getSelectAll() {
        return selectAll.clone();
    }

    public Sql getDelete() {
        return delete.clone();
    }
}
