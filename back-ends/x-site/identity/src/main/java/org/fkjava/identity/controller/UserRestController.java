package org.fkjava.identity.controller;

import org.fkjava.common.data.domain.Result;
import org.fkjava.identity.domain.User;
import org.fkjava.identity.service.IdentityService;
import org.fkjava.identity.vo.AutoCompleteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/rest/identity/user")
@SessionAttributes({"modifyUserId"})
public class UserRestController {

    private final IdentityService identityService;

    @Autowired
    public UserRestController(IdentityService identityService) {
        this.identityService = identityService;
    }

    // Model是通常放到方法参数列表中的，用于控制器和JSP传值，也可以作为方法返回值
    // View只是用来返回页面，可以作为返回值
    // ModelAndView是方法返回值，包含了数据和视图
    @GetMapping
    public Page<User> index(//
                            @RequestParam(name = "pageNumber", defaultValue = "0") Integer number, // 页码
                            @RequestParam(name = "keyword", required = false) String keyword// 搜索的关键字
                            //
    ) {
        // 查询一页的数据

        return identityService.findUsers(keyword, number);
    }

    /**
     * 根据名称查询用户信息列表，只返回姓名和id，用于输入框的自动完成
     *
     * @param keyword 查询用户信息的关键字
     * @return 自动完成的响应数据对象
     */
    @GetMapping(produces = "application/json")
    @ResponseBody
    public AutoCompleteResponse likeName(//
                                         @RequestParam(name = "query") String keyword// 搜索的关键字
                                         //
    ) {
        List<User> users = identityService.findUsers(keyword);

        List<User> result = new LinkedList<>();
        users.forEach(user -> {
            User u = new User();
            u.setId(user.getId());
            u.setName(user.getName());
            result.add(u);
        });
        return new AutoCompleteResponse(result);
    }

    @PostMapping
    public Result save(User user, //
                       // 从Session里面获取要修改的用户的ID
                       @SessionAttribute(value = "modifyUserId", required = false) String modifyUserId, //
                       SessionStatus sessionStatus//
    ) {
        // 修改用户的时候，把用户的ID设置到User对象里面
        if (modifyUserId != null
                // user对象没有id表示新增，新增的时候不需要id
                && !StringUtils.isEmpty(user.getId())) {
            user.setId(modifyUserId);
        }
        Result result = identityService.save(user);

        // 清理现场、Session里面的modifyUserId
        sessionStatus.setComplete();

        return result;
    }

    @GetMapping("/{id}")
    public User detail(@PathVariable("id") String id, Model model) {

        User user = this.identityService.findUserById(id);
        // 把要修改的用户的ID存储在Session里面，避免在浏览器恶意修改！
        model.addAttribute("modifyUserId", user.getId());

        return user;
    }

    @GetMapping("/active/{id}")
    public String active(@PathVariable("id") String id) {
        this.identityService.active(id);
        return "redirect:/identity/user";
    }

    @GetMapping("/disable/{id}")
    public String disable(@PathVariable("id") String id) {
        this.identityService.disable(id);
        return "redirect:/identity/user";
    }
}
