package org.fkjava.category.service.impl;

import org.fkjava.category.domain.Category;
import org.fkjava.category.repository.CategoryRepository;
import org.fkjava.category.service.CategoryService;
import org.fkjava.common.data.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class CategoryServiceImpl implements CategoryService, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void save(Category category) {
        if (StringUtils.isEmpty(category.getId())) {
            category.setId(null);
        }
        if (category.getParent() != null && StringUtils.isEmpty(category.getParent().getId())) {
            // 上级类目的id为null，表示没有上级类目
            category.setParent(null);
        }

        // 1.检查相同的父类目里面，是否有同名的子类目
        // 比如系统管理下面，只能有一个【用户管理】
        Category old;
        if (category.getParent() != null) {
            // 有上级类目，根据上级类目检查是否有重复
            old = this.categoryRepository.findByNameAndParent(category.getName(), category.getParent());
        } else {
            // 如果没有上级，则直接找parent_id为null的，检查是否有重复
            old = this.categoryRepository.findByNameAndParentNull(category.getName());
        }

        if (old != null && !old.getId().equals(category.getId())) {
            // 根据名称查询到数据库里面的类目，但是两者的id不同
            throw new IllegalArgumentException("类目的名字不能重复");
        }


        // 3.设置排序的序号（类目可以拖动顺序）
        // 找到同级最大的number，然后加10000000，就形成一个新的number作为当前类目的number
        // 如果是修改，则不需要查询
        if (old != null) {
            category.setNumber(old.getNumber());
        } else {
            Double maxNumber;
            if (category.getParent() == null) {
                maxNumber = this.categoryRepository.findMaxNumberByParentNull();
            } else {
                maxNumber = this.categoryRepository.findMaxNumberByParent(category.getParent());
            }
            if (maxNumber == null) {
                maxNumber = 0.0;
            }
            Double number = maxNumber + 10000000.0;
            category.setNumber(number);
        }

        // 4.保存数据
        this.categoryRepository.save(category);
    }

    @Override
    public List<Category> findTops() {
        return categoryRepository.findByParentNullOrderByNumber();
    }

    @Override
    public List<Category> findByParentId(String parentId) {
        if (StringUtils.isEmpty(parentId)) {
            return this.findTops();
        }
        Category parent = new Category();
        parent.setId(parentId);

        return categoryRepository.findByParent(parent);
    }

    @Override
    public Result move(String id, String targetId, String moveType) {
        Category category = this.categoryRepository.findById(id).orElse(null);
        if (category == null) {
            LOG.trace("移动类目出现问题: 无法根据id参数找到Category对象！");
            return Result.error("移动类目出现问题：参数错误！");
        }

        // 移动的重点：重新计算number（排序号），并且要修改parent

        if (StringUtils.isEmpty(targetId)) {
            // 一定是移动到所有一级类目的最后面
            Double maxNumber = this.categoryRepository.findMaxNumberByParentNull();
            if (maxNumber == null) {
                maxNumber = 0.0;
            }
            Double number = maxNumber + 10000000.0;
            category.setNumber(number);
            category.setParent(null);

            return Result.ok();
        }

        Category target = this.categoryRepository.findById(targetId).orElse(null);
        if (target == null) {
            LOG.trace("移动类目出现问题: 无法根据targetId参数找到Category对象！");
            return Result.error("移动类目出现问题：参数错误！");
        }
        if ("inner".equals(moveType)) {
            // 把类目移动到target里面，此时类目的parent直接改为target即可
            // number则是根据target作为父类目，找到最大的number，然后加上一个数字

            Double maxNumber = this.categoryRepository.findMaxNumberByParent(target);
            if (maxNumber == null) {
                maxNumber = 0.0;
            }
            Double number = maxNumber + 10000000.0;

            category.setParent(target);
            category.setNumber(number);
        } else if ("prev".equals(moveType)) {

            // number应该小于target的number，并且大于target前一个类目的number
            PageRequest pageable = PageRequest.of(0, 1);// 查询第一页、只要1条数据
            Page<Category> prevs = this.categoryRepository//
                    .findByParentAndNumberLessThanOrderByNumberDesc(target.getParent(), target.getNumber(), pageable);

            Double next = target.getNumber();
            double number;
            if (prevs.getNumberOfElements() > 0) {
                Double prev = prevs.getContent().get(0).getNumber();
                number = (next + prev) / 2;
            } else {
                number = next / 2;
            }
            category.setNumber(number);
            // 移动到target之前，跟target同级
            category.setParent(target.getParent());
        } else if ("next".equals(moveType)) {

            // number应该大于target的number，并且小于target后一个类目的number
            PageRequest pageable = PageRequest.of(0, 1);// 查询第一页、只要1条数据
            Page<Category> prevs = this.categoryRepository//
                    .findByParentAndNumberGreaterThanOrderByNumberAsc(target.getParent(), target.getNumber(), pageable);

            Double prev = target.getNumber();
            double number;
            if (prevs.getNumberOfElements() > 0) {
                Double next = prevs.getContent().get(0).getNumber();
                number = (next + prev) / 2;
            } else {
                number = prev + 10000000.0;
            }
            category.setNumber(number);
            // 移动到target之后，跟target同级
            category.setParent(target.getParent());
        } else {
            //throw new IllegalArgumentException("非法的类目移动类型，只允许inner、prev、next三选一。");
            return Result.error("非法的移动类型，只允许inner、prev、next三选一。");
        }

        this.categoryRepository.save(category);
        return Result.ok();
    }

    @Override
    public Result delete(String id) {
        Category entity = this.categoryRepository.findById(id).orElse(null);
        if (entity != null) {
            if (entity.getChildren().isEmpty()) {
                this.categoryRepository.delete(entity);
            } else {
                return Result.error();
            }
        }

        return Result.ok();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // 保证数据库里面最少一些一级类名，比如：视频、班级、文章
        List<Category> categories = this.categoryRepository.findByNameInAndParentIsNull("视频", "班级", "文章");
        checkCategory(categories, "视频");
        checkCategory(categories, "班级");
        checkCategory(categories, "文章");
    }

    private void checkCategory(List<Category> categories, String name) {
        categories.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .or(() -> {
                    Category c = new Category();
                    c.setName(name);
                    this.save(c);
                    return Optional.of(c);
                });
    }
}
