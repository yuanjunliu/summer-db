package cn.juns.summer.db.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ContentValues {
    private static final Logger LOG = LoggerFactory.getLogger(ContentValues.class);
    private final Map<EntityField, Object> mValues = new LinkedHashMap<>(8);
    private Long id;

    public ContentValues(Long id) {
        this.id = id;
    }

    public static ContentValues updateById(Long id) {
        return new ContentValues(id);
    }

    public Long getId() { return id; }

    public final void setId(Long id) {
        this.mValues.put(new EntityField("id", "id", Long.class, false, -1), id);
        this.id = id;
    }

    public ContentValues() {}

    public Object getValueByColumnName(String columnName) {
        Optional<Map.Entry<EntityField, Object>> opt = this.mValues.entrySet().stream()
                .filter(item -> item.getKey().getColumnName().equals(columnName)).findFirst();
        if (opt.isPresent()) {
            return opt.get().getValue();
        } else {
            return null;
        }
    }

    public Object getValueByFieldName(String fieldName) {
        Optional<Map.Entry<EntityField, Object>> opt = this.mValues.entrySet().stream()
                .filter(item -> item.getKey().getFieldClass().equals(fieldName)).findFirst();
        if (opt.isPresent()) {
            return opt.get().getValue();
        } else {
            return null;
        }
    }

    public Map<String, Object> toFieldsMap() {
        Map<String, Object> map = new LinkedHashMap();
        Iterator var2 = this.valueSet().iterator();

        while(var2.hasNext()) {
            Map.Entry<EntityField, Object> entry = (Map.Entry)var2.next();
            map.put(((EntityField)entry.getKey()).getFieldName(), entry.getValue());
        }

        return map;
    }

    public Map<String, Object> toColumnsMap() {
        Map<String, Object> map = new LinkedHashMap();
        Iterator var2 = this.valueSet().iterator();

        while(var2.hasNext()) {
            Map.Entry<EntityField, Object> entry = (Map.Entry)var2.next();
            map.put(((EntityField)entry.getKey()).getColumnName(), entry.getValue());
        }

        return map;
    }

    public boolean equals(Object object) {
        return !(object instanceof ContentValues) ? false : this.mValues.equals(((ContentValues)object).mValues);
    }

    public int hashCode() {
        return this.mValues.hashCode();
    }

    public void putAll(ContentValues other) {
        this.mValues.putAll(other.mValues);
    }

    public ContentValues put(EntityField field, Object value) {
        this.mValues.put(field, value);
        return this;
    }

    public int size() {
        return this.mValues.size();
    }

    public void remove(EntityField key) {
        this.mValues.remove(key);
    }

    public void clear() {
        this.mValues.clear();
    }

    public boolean containsKey(EntityField field) {
        return this.mValues.containsKey(field);
    }

    public Object get(EntityField key) {
        return this.mValues.get(key);
    }

    public Set<Map.Entry<EntityField, Object>> valueSet() {
        return this.mValues.entrySet();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        EntityField name;
        Object value;
        for(Iterator var2 = this.mValues.keySet().iterator(); var2.hasNext(); sb.append(name).append("=").append(value)) {
            name = (EntityField)var2.next();
            value = this.get(name);
            if (sb.length() > 0) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}
