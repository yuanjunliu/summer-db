package cn.juns.summer.db;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StorageManager {
    private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);
    protected int deleteBatchMaxSize = 100;
    protected int insertBatchMaxSize = 10000;
    private boolean disabled;
    private int resetInterval = 60;
    private int maxErrorCount = 600;
    private int maxSlowCount = 200;
    private long minSlowTime = 1000L;
    private Stat stat = new Stat();
    private String name;

    public StorageManager() {}

    void addErrorCount(StorageManager.SqlLine line) {
        this.resetIfNeed();
        ++this.stat.errorCount;
        ++this.stat.executeCount;
        this.stat.executeTime += line.usedTime;
        this.stat.errorSqlList.add(line);
        if (this.stat.errorCount > this.maxErrorCount) {
            this.disabled = true;
        }

    }

    void addExecuteCount(StorageManager.SqlLine line) {
        this.resetIfNeed();
        if (line.usedTime > this.minSlowTime) {
            ++this.stat.slowCount;
            this.stat.slowSqlList.add(line);
            if (this.stat.slowCount > this.maxSlowCount) {
                this.disabled = true;
            }
        }

        ++this.stat.executeCount;
        this.stat.executeTime += line.usedTime;
    }

    private void resetIfNeed() {
        if (System.currentTimeMillis() - this.stat.lastResetTime > (long)this.resetInterval * 1000L) {
            StorageManager.Stat local = this.stat;
            this.stat = new StorageManager.Stat();
            this.doLog(local);
            this.disabled = false;
        }

    }

    private void doLog(StorageManager.Stat stat) {
        DecimalFormat df = new DecimalFormat("0.00");
        LOG.info("【{}】:errorCount={},slowCount={},executeCount={},allTime={}ms,avgTime={}ms,blockCount={}", new Object[]{this.name, stat.errorCount, stat.slowCount, stat.executeCount, stat.executeTime, df.format((double)((float)stat.executeTime * 1.0F / (float)stat.executeCount)), stat.blockCount});
        if (stat.errorSqlList.isEmpty() || stat.slowSqlList.isEmpty()) {
            LOG.error("=====================================================================================================");
            Iterator var3 = stat.errorSqlList.iterator();

            StorageManager.SqlLine line;
            while(var3.hasNext()) {
                line = (StorageManager.SqlLine)var3.next();
                LOG.error("[ERROR]: {} - {} - {} - {}", new Object[]{(new DateTime(line.createTime)).toString("yyyy-MM-dd HH:mm:ss.SSSS"), line.usedTime, line.sql, line.errorInfo});
            }

            var3 = stat.slowSqlList.iterator();

            while(var3.hasNext()) {
                line = (StorageManager.SqlLine)var3.next();
                LOG.error("[SLOW]: {} - {} - {}", new Object[]{(new DateTime(line.createTime)).toString("yyyy-MM-dd HH:mm:ss.SSSS"), line.usedTime, line.sql});
            }
        }

    }

    public boolean isAvailable() {
        this.resetIfNeed();
        if (this.disabled) {
            ++this.stat.blockCount;
            return false;
        } else {
            return true;
        }
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getResetInterval() {
        return this.resetInterval;
    }

    public void setResetInterval(int resetInterval) {
        this.resetInterval = resetInterval;
    }

    public int getMaxErrorCount() {
        return this.maxErrorCount;
    }

    public void setMaxErrorCount(int maxErrorCount) {
        this.maxErrorCount = maxErrorCount;
    }

    public int getMaxSlowCount() {
        return this.maxSlowCount;
    }

    public void setMaxSlowCount(int maxSlowCount) {
        this.maxSlowCount = maxSlowCount;
    }

    public long getMinSlowTime() {
        return this.minSlowTime;
    }

    public void setMinSlowTime(long minSlowTime) {
        this.minSlowTime = minSlowTime;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInsertBatchMaxSize() {
        return this.insertBatchMaxSize;
    }

    public void setInsertBatchMaxSize(int insertBatchMaxSize) {
        this.insertBatchMaxSize = insertBatchMaxSize;
    }

    public int getDeleteBatchMaxSize() {
        return this.deleteBatchMaxSize;
    }

    public void setDeleteBatchMaxSize(int deleteBatchMaxSize) {
        this.deleteBatchMaxSize = deleteBatchMaxSize;
    }

//    public DbUnavailableHandler getDbUnavailableHandler() {
//        return this.dbUnavailableHandler;
//    }
//
//    public void setDbUnavailableHandler(DbUnavailableHandler dbUnavailableHandler) {
//        this.dbUnavailableHandler = dbUnavailableHandler;
//    }

    static class SqlLine{
        String errorInfo;
        String sql;
        long createTime = System.currentTimeMillis();
        long usedTime;
        SqlLine() {}
    }

    static class Stat{
        long lastResetTime = System.currentTimeMillis();
        int errorCount;
        int slowCount;
        int executeCount;
        long executeTime;
        List<SqlLine> slowSqlList = new ArrayList<>();
        List<SqlLine> errorSqlList = new ArrayList<>();
        int blockCount;
        Stat() {}
    }
}
