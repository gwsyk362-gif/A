package assistant.service.impl;

import assistant.entity.PracticeRecord;
import assistant.entity.Question;
import assistant.entity.SubjectCoverageDTO;
import assistant.entity.UserSubjectScore;
import assistant.mapper.PracticeRecordMapper;
import assistant.mapper.QuestionMapper;
import assistant.mapper.UserSubjectScoreMapper;
import assistant.service.PracticeRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PracticeRecordServiceimpl extends ServiceImpl<PracticeRecordMapper, PracticeRecord> implements PracticeRecordService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserSubjectScoreMapper userSubjectScoreMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRecordsAndUpdateScore(List<PracticeRecord> records) {
        if (records == null || records.isEmpty()) return false;

        // 1. 保存这次答题记录
        this.saveBatch(records);

        for (PracticeRecord record : records) {
            Integer userId = record.getRecUserId();
            Integer quesId = record.getRecQuesId();

            Question question = questionMapper.selectById(quesId);
            if (question == null) continue;

            Integer subId = question.getQuesSubId();
            double quesDifficulty = question.getQuesScore() != null ? question.getQuesScore() : 1500.0;

            // 查找用户该科目的当前能力分
            UserSubjectScore uss = userSubjectScoreMapper.selectOne(
                    new QueryWrapper<UserSubjectScore>()
                            .eq("user_id", userId)
                            .eq("sub_id", subId)
            );

            if (uss == null) {
                uss = new UserSubjectScore();
                uss.setUserId(userId);
                uss.setSubId(subId);
                uss.setEloScore(1500);
                uss.setUpdateTime(LocalDateTime.now());
                userSubjectScoreMapper.insert(uss);
            }

            // --- Elo 算法核心逻辑 ---
            double userCurrentScore = uss.getEloScore();
            // 用户期望胜率
            double expectedWinRate = 1.0 / (1.0 + Math.pow(10, (quesDifficulty - userCurrentScore) / 400.0));
            // 用户实际得分
            double userActualScore = record.getRecIsCorrect() == 1 ? 1.0 : 0.0;

            int K = 32; // 变化系数

            // A. 更新用户分数
            int newUserScore = (int) Math.round(userCurrentScore + K * (userActualScore - expectedWinRate));
            uss.setEloScore(newUserScore);
            uss.setUpdateTime(LocalDateTime.now());
            userSubjectScoreMapper.updateById(uss);

            // B. 更新题目难度 (新增逻辑)
            // 题目期望胜率 = 1 - 用户期望胜率
            double quesExpectedWinRate = 1.0 - expectedWinRate;
            // 题目实际得分 = 1 - 用户实际得分 (用户对则题败，用户错则题胜)
            double quesActualScore = 1.0 - userActualScore;

            // 计算新难度
            int newDifficulty = (int) Math.round(quesDifficulty + K * (quesActualScore - quesExpectedWinRate));

            // 写回题目表
            question.setQuesScore(newDifficulty);
            questionMapper.updateById(question);
        }
        return true;
    }

    @Override
    public List<Map<String, Object>> getKnowledgeMastery(Integer userId) {
        return baseMapper.getKnowledgeMastery(userId);
    }

    @Override
    public List<Map<String, Object>> getDailyStats(Integer userId) {
        return baseMapper.getDailyStats(userId);
    }

    @Override
    public List<Map<String, Object>> getErrorRecords(Integer userId) {
        return baseMapper.getErrorRecords(userId);
    }

    @Override
    public List<Map<String, Object>> getYearlyStats(Integer userId) {
        return baseMapper.getYearlyStats(userId);
    }

    @Override
    public List<SubjectCoverageDTO> getSubjectCoverage(Integer userId) {return baseMapper.getSubjectCoverage(userId);}
}