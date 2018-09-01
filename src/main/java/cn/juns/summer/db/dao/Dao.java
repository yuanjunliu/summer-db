package cn.juns.summer.db.dao;

import cn.juns.summer.db.PageRequest;
import cn.juns.summer.db.PageResponse;
import cn.juns.summer.db.SearchFilter;
import cn.juns.summer.db.Sql;
import cn.juns.summer.db.condition.SqlPath;

import java.util.List;

public interface Dao<T, ID> {
    boolean insert(T t);
    boolean insert(ContentValues cv);
    void insertBatch(List<T> list);
    boolean update(T t);
    boolean update(ContentValues cv);
    boolean deleteById(ID id);
    boolean delete(T t);
    int deleteBatch(List<ID> list);
    T findById(ID id);
    T findOne(SqlPath sqlPath);
    <S> S findOne(SqlPath sqlPath, Class<S> clazz);
    List<T> findAll(SqlPath sqlPath);
    List<T> findAll(Sql sql);
    <S> List<S> findAll(SqlPath sqlPath, Class<S> clazz);
    <S> List<S> listBySql(Sql sql, Class<S> clazz);
    <S> PageResponse<S> listAll(Sql sql, PageRequest pageRequest, Class<S> clazz);
    PageResponse<T> listAll(SqlPath sqlPath);
    PageResponse<T> listAll(SqlPath sqlPath, PageRequest pageRequest);
    PageResponse<T> listAll(PageRequest pageRequest);
    List<T> listAllWithoutPageInfo(PageRequest pageRequest);
    PageResponse<T> listAll(List<SearchFilter> searchFilters, PageRequest pageRequest);
    List<T> listAllWithoutPageInfo(List<SearchFilter> searchFilters, PageRequest pageRequest);
    long count();
    long count(Sql sql);
    long count(SqlPath sqlPath);
    long count(List<SearchFilter> searchFilters);

}
