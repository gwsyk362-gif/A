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

    // 登录
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername())
                .eq("password", user.getPassword());
        return userService.getOne(queryWrapper);
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
}