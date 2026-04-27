package assistant.service;

import assistant.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    User login(Integer userId);

    boolean register(User user);
}