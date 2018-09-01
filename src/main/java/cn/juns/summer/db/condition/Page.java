package cn.juns.summer.db.condition;

public class Page {
    private long offset;
    private int page = 1;
    private long limit = 0L;

    public Page() {}

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        if (offset >= 0L) {
            this.offset = offset;
        } else {
            this.offset = 0L;
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        if (page >= 1) {
            this.page = page;
        } else {
            this.page = 1;
        }
        this.offset = (long) (this.page -1 ) * this.limit;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
