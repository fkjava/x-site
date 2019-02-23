package org.fkjava.identity.controller;

import org.fkjava.common.data.domain.Result;
import org.fkjava.identity.domain.Role;
import org.fkjava.identity.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/identity/role")
public class RoleRestController {

    private final RoleService roleService;

    @Autowired
    public RoleRestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("notFixed")
    public List<Role> findAllNotFixed() {
        return this.roleService.findAllNotFixed();
    }

    @GetMapping
    public List<Role> index() {
        return roleService.findAllRoles();
    }

    @PostMapping
    public Result save(Role role) {
        return roleService.save(role);
    }

    @DeleteMapping("{id}")
    @ResponseBody
    public Result delete(@PathVariable("id") String id) {
        return this.roleService.deleteById(id);
    }
}
