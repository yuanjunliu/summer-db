package cn.juns.summer.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 200140725L;
    private long total;
    private List<T> itemList = new ArrayList<>();

    public PageResponse() {}

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getItemList() {
        return itemList;
    }

    public void setItemList(List<T> itemList) {
        this.itemList = itemList;
    }
}
