package assistant.service.impl;

import assistant.entity.LoginLog;
import assistant.entity.User;
import assistant.mapper.LoginLogMapper;
import assistant.mapper.UserMapper;
import assistant.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private LoginLogMapper loginLogMapper;   // 注入日志 Mapper

    @Override
    public User login(String username, String password) {
        // 1. 查询用户（这里假设密码是明文，实际应该加密比较）
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", username).eq("password", password);
        User user = this.getOne(query);

        // 2. 如果查到用户，记录登录日志（若业务需要，可在此判断用户状态等）
        if (user != null) {
            LoginLog log = new LoginLog();
            log.setUserId(user.getUserId());
            log.setLoginTime(LocalDateTime.now());
            log.setLoginDate(LocalDate.now());
            loginLogMapper.insert(log);
        }
        return user;
    }

    @Override
    public boolean register(User user) {
        return this.save(user);
    }
}