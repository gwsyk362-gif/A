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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/mock")
public class MockTrainingController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PracticeRecordService practiceRecordService;

    /**
     * 访问 http://localhost:8080/api/mock/run 触发训练
     */
    @GetMapping("/run")
    public String startSimulation() {
        // 1. 获取所有用户和题目[cite: 1, 3]
        List<User> users = userMapper.selectList(null);
        List<Question> questions = questionMapper.selectList(null);

        if (users.isEmpty() || questions.isEmpty()) {
            return "失败：数据库中用户或题目表为空，无法训练！";
        }

        Random random = new Random();
        int totalLoops = 600; // 模拟答题
        int successCount = 0;

        for (int i = 0; i < totalLoops; i++) {
            // 2. 随机抽取用户和题目[cite: 7]
            User user = users.get(random.nextInt(users.size()));
            Question ques = questions.get(random.nextInt(questions.size()));

            // 3. 设定模拟规则
            Random random1 = new Random();
            double userVirtualAbility = 1500 + random1.nextGaussian() * 200;
            double quesVirtualDifficulty =ques.getQuesScore();

            // 计算在此模拟设定下的“真实”做对概率
            double probability = 1.0 / (1.0 + Math.pow(10, (quesVirtualDifficulty - userVirtualAbility) / 400.0));
            boolean isCorrect = random.nextDouble() < probability;

            // 4. 封装记录
            PracticeRecord record = new PracticeRecord();
            record.setRecUserId(user.getUserId());
            record.setRecQuesId(ques.getQuesId());
            record.setRecIsCorrect(isCorrect ? 1 : 0);
            record.setRecTime(LocalDateTime.now());
            record.setRecUserAnswer("模拟系统自动答题");

            List<PracticeRecord> recordList = new ArrayList<>();
            recordList.add(record);

            try {
                practiceRecordService.saveRecordsAndUpdateScore(recordList);
                successCount++;
            } catch (Exception e) {
                // 记录失败日志
            }
        }

        return "训练任务执行完毕！成功注入 " + successCount + " 条模拟数据。";
    }
}