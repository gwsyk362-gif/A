package assistant.controller;

import assistant.entity.LoginLog;
import assistant.entity.User;
import assistant.mapper.LoginLogMapper;
import assistant.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// 访问 http://localhost:8080/api/mock/login 触发生成
@RestController
@RequestMapping("/api/mock")
public class MockLoginController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @GetMapping("/login")
    public String generateLoginData() {
        // 1. 获取所有用户，并过滤掉 admin
        List<User> allUsers = userMapper.selectList(null);
        if (allUsers.isEmpty()) {
            return "失败：用户表为空！";
        }

        List<User> regularUsers = allUsers.stream()
                .filter(u -> !"admin".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());

        if (regularUsers.isEmpty()) {
            return "失败：没有用户！";
        }

        Random random = new Random();
        int totalLogs = 1000;   // 想生成的登录记录总数
        int successCount = 0;
        LocalDate today = LocalDate.now();

        for (int i = 0; i < totalLogs; i++) {
            // 2. 从普通用户中随机抽取
            User user = regularUsers.get(random.nextInt(regularUsers.size()));

            // 3. 生成近30天内的随机日期和时间
            int daysAgo = random.nextInt(30);
            LocalDate loginDate = today.minusDays(daysAgo);
            int hour = random.nextInt(24);
            int minute = random.nextInt(60);
            int second = random.nextInt(60);
            LocalDateTime loginTime = LocalDateTime.of(loginDate, LocalTime.of(hour, minute, second));

            // 4. 封装并保存
            LoginLog log = new LoginLog()
                    .setUserId(user.getUserId())
                    .setLoginTime(loginTime)
                    .setLoginDate(loginDate);

            try {
                loginLogMapper.insert(log);
                successCount++;
            } catch (Exception e) {
                System.err.println("插入日志失败: " + e.getMessage());
            }
        }

        return "登录数据生成完毕！成功生成 " + successCount + " 条记录。";
    }
}