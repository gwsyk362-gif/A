package assistant.controller;

import assistant.entity.User;
import assistant.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    // 登录接口
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        // 登录逻辑：根据 username 和 password 查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername())
                .eq("password", user.getPassword());
        return userService.getOne(queryWrapper);
    }

    // 注册接口
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        // 二次校验账号长度（安全性考虑）
        if (user.getUsername() == null || String.valueOf(user.getUsername()).length() != 8) {
            return "Fail: Account must be 8 digits";
        }

        // 检查账号是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (userService.getOne(queryWrapper) != null) {
            return "Fail: Account already exists";
        }

        // 调用 MyBatis-Plus 的 save 方法写入数据库
        boolean success = userService.save(user);

        return success ? "Success" : "Fail";
    }
}