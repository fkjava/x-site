package org.fkjava.category.repository;

import org.fkjava.category.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByParentNullOrderByNumber();

    List<Category> findByParent(Category parent);

    @Query("select max(number) from Category where parent is null")
    Double findMaxNumberByParentNull();

    @Query("select max(number) from Category where parent = :parent")
    Double findMaxNumberByParent(@Param("parent") Category target);

    //@Query("from Category c where c.parent = :parent and c.number < :number order by c.number desc")
    Page<Category> findByParentAndNumberLessThanOrderByNumberDesc(
            @Param("parent") Category parent, @Param("number") Double number, Pageable pageable);

    //@Query("from Category c where c.parent = :parent and c.number > :number order by c.number asc")
    Page<Category> findByParentAndNumberGreaterThanOrderByNumberAsc(
            @Param("parent") Category parent, @Param("number") Double number, Pageable pageable);

    Category findByNameAndParent(String name, Category parent);

    Category findByNameAndParentNull(String name);

    List<Category> findByNameInAndParentIsNull(String... names);
}
