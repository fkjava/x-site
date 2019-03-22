package org.fkjava.category.service;

import org.fkjava.category.domain.Category;
import org.fkjava.common.data.domain.Result;

import java.util.List;

public interface CategoryService {


    void save(Category category);

    /**
     * 查询所有的一级类目，并且级联所有次级目录
     *
     * @return 返回的集合里面全是一级类目，但是可以通过调用{@link Category#getChildren()}得到所有的次级类目。
     */
    List<Category> findTops();

    /**
     * 根据上级类目的ID返回所有的次级类目，而且级联递归所有次级类目。
     *
     * @param parentId 上级类目的ID
     * @return 以列表的形式返回所有的次级类目，可以通过调用{@link Category#getChildren()}得到所有的子孙后代类目。
     */
    List<Category> findByParentId(String parentId);

    /**
     * 移动节点，可能把指定节点移动到目标节点的前面、后面、里面
     *
     * @param id       要移动的节点
     * @param targetId 目标节点的ID
     * @param moveType 支持的移动类型为：inner、prev、next
     * @return 返回移动结果
     */
    Result move(String id, String targetId, String moveType);

    /**
     * 删除节点，如果有次级节点不允许删除。
     *
     * @param id 要删除的节点的ID
     * @return 如果没有次级节点返回删除成功，否则返回删除失败。
     */
    Result delete(String id);

}
