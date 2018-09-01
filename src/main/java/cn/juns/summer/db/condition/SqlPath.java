package cn.juns.summer.db.condition;

import cn.juns.summer.db.PageRequest;
import cn.juns.summer.db.SearchFilter;
import cn.juns.summer.db.dao.EntityField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlPath {
    private Node node = null;
    private List<Order> orders = null;
    private Page page = null;
    private List<SqlField> selectFields;

    private SqlPath() {
    }

    public SqlPath(Node node) {
        this.node = node;
    }

    public static SqlPath where(EntityField field, SearchFilter.Operator op, Object value) {
        return new SqlPath(Node.create(field, op, value));
    }

    public static SqlPath orderBy() {
        return new SqlPath();
    }

    public static SqlPath select(EntityField... fields) {
        SqlPath sqlPath = new SqlPath();
        if (fields.length > 0) {
            sqlPath.selectFields = new ArrayList<>(fields.length);
            Arrays.stream(fields).forEach(field -> {
                sqlPath.selectFields.add(new SqlField(field));
            });
        }
        return sqlPath;
    }

    public SqlPath and(EntityField field, SearchFilter.Operator op, Object value) {
        if (this.node == null) {
            this.node = Node.create(field, op, value);
        } else {
            this.node = new Nodes(this.node, PathType.AND, Node.create(field, op, value));
        }
        return this;
    }

    public SqlPath or(EntityField field, SearchFilter.Operator op, Object value) {
        if (this.node == null) {
            this.node = Node.create(field, op, value);
        } else {
            this.node = new Nodes(this.node, PathType.OR, Node.create(field, op, value));
        }
        return this;
    }

    public SqlPath and(SqlPath sp) {
        if (sp.node != null) {
            if (this.node != null) {
                this.node = new Nodes(this.node, PathType.AND, sp.node);
            } else {
                this.node = sp.node;
            }
        }
        return this;
    }

    public SqlPath or(SqlPath sp) {
        if (sp.node != null) {
            if (this.node != null) {
                this.node = new Nodes(this.node, PathType.OR, sp.node);
            } else {
                this.node = sp.node;
            }
        }
        return this;
    }

    public SqlPath asc(EntityField field) {
        if (this.orders == null) {
            this.orders = new ArrayList<>(5);
        }
        this.orders.add(new Order(field.getFieldName(), PageRequest.Direction.ASC.name()));
        return this;
    }

    public SqlPath asc(String fieldName) {
        if (this.orders == null) {
            this.orders = new ArrayList(5);
        }

        this.orders.add(new Order(fieldName, "ASC"));
        return this;
    }

    public SqlPath desc(EntityField field) {
        if (this.orders == null) {
            this.orders = new ArrayList(5);
        }

        this.orders.add(new Order(field.getFieldName(), "DESC"));
        return this;
    }

    public SqlPath desc(String fieldName) {
        if (this.orders == null) {
            this.orders = new ArrayList(5);
        }

        this.orders.add(new Order(fieldName, "DESC"));
        return this;
    }

    public SqlPath page(PageRequest request) {
        this.page(request.getPage(), request.getLimit());

        return this;
    }

    public SqlPath page(int pageNumber, int pageSize) {
        if (this.page == null) {
            this.page = new Page();
        }
        this.page.setLimit((long) pageSize);
        this.page.setPage(pageNumber);
        return this;
    }

    public Page getPage() {
        return page;
    }

    public Node getNode() {
        return node;
    }


    public List<Order> getOrders() {
        return orders;
    }

    public List<SqlField> getSelectFields() {
        return selectFields;
    }
}
