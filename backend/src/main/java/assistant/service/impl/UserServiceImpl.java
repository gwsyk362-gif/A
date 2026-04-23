package assistant.service.impl;

import assistant.entity.User;
import assistant.mapper.UserMapper;
import assistant.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service; // 必须引入这个

@Service // 关键：告诉 Spring 这是一个 Service 组件
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(Integer userId) {
        return this.getById(userId);
    }

    @Override
    public boolean register(User user) {
        return this.save(user);
    }
}