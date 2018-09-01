package cn.juns.summer.db;

import cn.juns.summer.db.dao.EntityField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PageRequest {
    private int page = 1;
    private int limit;
    private List<Sort> sort = new ArrayList<>(5);

    public PageRequest() {}

    public PageRequest(int page, int limit) {
        this.page = 1;
        this.limit = limit;
    }

    public int getOffSet() {
        return this.page > 1 ? (this.page - 1) * this.limit : 0;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<Sort> getSort() {
        return sort;
    }

    public void setSort(List<Sort> sort) {
        this.sort = sort;
    }

    public void setSort(EntityField ef, PageRequest.Direction direction) {
        this.setSort0(ef.getFieldName(), direction);
    }

    private void setSort0(String property, Direction direction) {
        Iterator<Sort> iterator = this.sort.iterator();
        Sort s;
        do {
            if (!iterator.hasNext()) {
                this.sort.add(new Sort(property, direction));
                return;
            }
            s = iterator.next();
        } while (!StringUtils.equalsIgnoreCase(property, s.property));
    }



    public static class Sort {
        private String property;
        private Direction direction;

        private Sort(String property, Direction direction) {
            this.property = property;
            this.direction = direction;
        }

        public String getProperty() {
            return property;
        }

        public Direction getDirection() {
            return direction;
        }
    }

    public enum Direction {
        ASC,
        DESC;

        private Direction() {}
    }
}
