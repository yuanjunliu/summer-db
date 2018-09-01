package cn.juns.summer.db.dao.impl;

import cn.juns.summer.db.*;
import cn.juns.summer.db.condition.SqlPath;
import cn.juns.summer.db.dao.BaseDao;
import cn.juns.summer.db.dao.ContentValues;
import cn.juns.summer.db.entity.IdEntity;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public class AbstractDao<T extends IdEntity> extends AbstractStorage implements BaseDao<T> {
    private CrudBuilder<T> crudBuilder;

    protected CrudBuilder<T> getBuilder() {
        if (crudBuilder == null) {
            crudBuilder = new CrudBuilder<>(getEntityClass());
        }
        return crudBuilder;
    }

    protected Class<T> getEntityClass() {
        ParameterizedType type = (ParameterizedType)this.getClass().getGenericSuperclass();
        return (Class) type.getActualTypeArguments()[0];
    }

    @Override
    public boolean insert(T t) {
        return this.execute(Sql.from(crudBuilder.getInsert().toString(), crudBuilder.buildInsertParams(t)));
    }

    @Override
    public boolean insert(ContentValues cv) {
        return this.execute(crudBuilder.buildInsertSql(cv));
    }

    @Override
    public void insertBatch(List<T> list) {

    }

    @Override
    public boolean update(T t) {
        return false;
    }

    @Override
    public boolean update(ContentValues cv) {
        return false;
    }

    @Override
    public boolean deleteById(Long aLong) {
        return false;
    }

    @Override
    public boolean delete(T t) {
        return false;
    }

    @Override
    public int deleteBatch(List<Long> list) {
        return 0;
    }

    @Override
    public T findById(Long aLong) {
        return null;
    }

    @Override
    public T findOne(SqlPath sqlPath) {
        return null;
    }

    @Override
    public <S> S findOne(SqlPath sqlPath, Class<S> clazz) {
        return null;
    }

    @Override
    public List<T> findAll(SqlPath sqlPath) {
        return null;
    }

    @Override
    public List<T> findAll(Sql sql) {
        return null;
    }

    @Override
    public <S> List<S> findAll(SqlPath sqlPath, Class<S> clazz) {
        return null;
    }

    @Override
    public <S> List<S> listBySql(Sql sql, Class<S> clazz) {
        return null;
    }

    @Override
    public <S> PageResponse<S> listAll(Sql sql, PageRequest pageRequest, Class<S> clazz) {
        return null;
    }

    @Override
    public PageResponse<T> listAll(SqlPath sqlPath) {
        return null;
    }

    @Override
    public PageResponse<T> listAll(SqlPath sqlPath, PageRequest pageRequest) {
        return null;
    }

    @Override
    public PageResponse<T> listAll(PageRequest pageRequest) {
        return null;
    }

    @Override
    public List<T> listAllWithoutPageInfo(PageRequest pageRequest) {
        return null;
    }

    @Override
    public PageResponse<T> listAll(List<SearchFilter> searchFilters, PageRequest pageRequest) {
        return null;
    }

    @Override
    public List<T> listAllWithoutPageInfo(List<SearchFilter> searchFilters, PageRequest pageRequest) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public long count(Sql sql) {
        return 0;
    }

    @Override
    public long count(SqlPath sqlPath) {
        return 0;
    }

    @Override
    public long count(List<SearchFilter> searchFilters) {
        return 0;
    }
}
