package assistant.service;

import assistant.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    // 登录逻辑：根据 ID 查询用户
    User login(Integer userId);

    // 注册逻辑：插入新用户
    boolean register(User user);
}