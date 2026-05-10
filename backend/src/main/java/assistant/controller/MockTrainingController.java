package assistant.controller;

import assistant.entity.PracticeRecord;
import assistant.entity.Question;
import assistant.entity.User;
import assistant.mapper.QuestionMapper;
import assistant.mapper.UserMapper;
import assistant.service.PracticeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mock")
public class MockTrainingController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PracticeRecordService practiceRecordService;

    // 访问 http://localhost:8080/api/mock/run 触发训练
    @GetMapping("/run")
    public String startSimulation() {
        // 1. 获取所有用户，并排除 admin 角色
        List<User> allUsers = userMapper.selectList(null);
        if (allUsers.isEmpty()) {
            return "失败：用户表为空！";
        }
        List<User> regularUsers = allUsers.stream()
                .filter(u -> !"admin".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
        if (regularUsers.isEmpty()) {
            return "失败：没有可用的非管理员用户！";
        }

        // 2. 获取所有题目
        List<Question> questions = questionMapper.selectList(null);
        if (questions.isEmpty()) {
            return "失败：题目表为空，无法训练！";
        }

        Random random = new Random();
        int totalLoops = 600;
        int successCount = 0;
        LocalDate today = LocalDate.now();

        for (int i = 0; i < totalLoops; i++) {
            // 3. 随机抽取普通用户和题目
            User user = regularUsers.get(random.nextInt(regularUsers.size()));
            Question ques = questions.get(random.nextInt(questions.size()));

            // 4. 模拟用户能力与题目难度，计算做对概率
            double userVirtualAbility = 1500 + random.nextGaussian() * 200;
            double quesVirtualDifficulty = ques.getQuesScore();
            double probability = 1.0 / (1.0 + Math.pow(10,
                    (quesVirtualDifficulty - userVirtualAbility) / 400.0));
            boolean isCorrect = random.nextDouble() < probability;

            // 5. 生成近30天内的随机做题时间
            int daysAgo = random.nextInt(30);
            LocalDate recordDate = today.minusDays(daysAgo);
            int hour = random.nextInt(24);
            int minute = random.nextInt(60);
            int second = random.nextInt(60);
            LocalDateTime recTime = LocalDateTime.of(recordDate,
                    LocalTime.of(hour, minute, second));

            // 6. 封装记录
            PracticeRecord record = new PracticeRecord();
            record.setRecUserId(user.getUserId());
            record.setRecQuesId(ques.getQuesId());
            record.setRecIsCorrect(isCorrect ? 1 : 0);
            record.setRecTime(recTime);
            record.setRecUserAnswer("模拟系统自动答题");

            List<PracticeRecord> recordList = new ArrayList<>();
            recordList.add(record);

            try {
                practiceRecordService.saveRecordsAndUpdateScore(recordList);
                successCount++;
            } catch (Exception e) {
                // 记录失败，继续下一次循环
            }
        }

        return "训练任务执行完毕！成功注入 " + successCount + " 条模拟数据。";
    }
}