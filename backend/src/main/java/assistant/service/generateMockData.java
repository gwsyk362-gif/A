package assistant.service;

import assistant.entity.Question;
import assistant.entity.Subject;
import assistant.mapper.QuestionMapper;
import assistant.mapper.SubjectMapper;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class generateMockData {

    @Autowired
    private  SubjectMapper subjectMapper;

    @Autowired
    private  QuestionMapper questionMapper;

    public generateMockData() {
    }
//SubjectMapper subjectMapper,QuestionMapper questionMapper
    @PostConstruct
    public void runAfterSpringIsReady() {
          if (subjectMapper.selectCount(null) > 0 || questionMapper.selectCount(null) > 0) {
            System.out.println("Mock data already exists, skip generation.");
            return;
        } else {
            String[] subjectNames = {"数学", "英语", "计算机组成原理", "操作系统", "数据结构"};
            Random random = new Random();

            for (String sName : subjectNames) {
                // 1. 保存科目
                Subject subject = new Subject();
                subject.setSubName(sName);
                subjectMapper.insert(subject);

                // 2. 为每个科目生成 10-20 道题目
                int questionCount = 10 + random.nextInt(11);
                for (int i = 0; i < questionCount; i++) {
                    Question question = new Question();

                    question.setQuesContent(sName + "测试题目 - " + (i + 1));
                    question.setQuesDifficulty(java.math.BigDecimal.valueOf(random.nextInt(5) + 1));
                    question.setQuesSubId(subject.getSubId());
                    question.setQuesAnswer("答案待定");

                    // 保存题目
                    questionMapper.insert(question);
                }
            }
        }
    }
}
