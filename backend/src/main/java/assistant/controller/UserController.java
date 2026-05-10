package assistant.controller;

import assistant.entity.User;
import assistant.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    // 登录
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        return userService.login(String.valueOf(user.getUsername()), user.getPassword());
    }

    // 注册
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (user.getUsername() == null || String.valueOf(user.getUsername()).length() != 8) {
            return "Fail: Account must be 8 digits";
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (userService.getOne(queryWrapper) != null) {
            return "Fail: Account already exists";
        }
        boolean success = userService.save(user);
        return success ? "Success" : "Fail";
    }

    // 获取所有非管理员用户
    @GetMapping("/allStudents")
    public List<User> getAllStudents() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("role", "admin");
        return userService.list(queryWrapper);
    }

    // 修改用户信息
    @PostMapping("/update")
    public boolean updateUser(@RequestBody User user) {
        return userService.updateById(user);
    }

    // 切换用户状态（封禁/解封）
    @PostMapping("/changeStatus")
    public boolean changeStatus(@RequestBody User user) {
        User updateEntity = new User();
        updateEntity.setUserId(user.getUserId());
        updateEntity.setStatus(user.getStatus());

        return userService.updateById(updateEntity);
    }

    // 分页搜索用户列表
    @GetMapping("/page")
    public Page<User> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        //创建分页对象
        Page<User> page = new Page<>(current, size);

        //构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("role", "admin");

        // 如果有关键字，模糊查询账号或昵称
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper ->
                    wrapper.like("username", keyword)
                            .or()
                            .like("nickname", keyword)
            );
        }

        //按用户ID降序排列
        queryWrapper.orderByAsc("user_id");

        return userService.page(page, queryWrapper);
    }
}