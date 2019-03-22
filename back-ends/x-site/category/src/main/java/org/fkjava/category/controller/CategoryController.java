package org.fkjava.category.controller;

import org.fkjava.category.domain.Category;
import org.fkjava.category.service.CategoryService;
import org.fkjava.common.data.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 如果客户端要求返回JSON的时候，调用下面这个方法
    @GetMapping()
    public List<Category> findTopMenus() {
        return this.categoryService.findTops();
    }

    @PostMapping
    public Result save(@RequestBody Category category) {
        this.categoryService.save(category);
        return Result.ok("保存成功");
    }

    @PostMapping("move")
    public Result move(String id, String targetId, String moveType) {
        return this.categoryService.move(id, targetId, moveType);
    }

    @DeleteMapping("{id}")
    public Result delete(@PathVariable("id") String id) {
        return this.categoryService.delete(id);
    }
}
