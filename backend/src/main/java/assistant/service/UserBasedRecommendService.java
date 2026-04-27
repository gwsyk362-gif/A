package assistant.service;

import assistant.entity.PracticeRecord;
import assistant.mapper.PracticeRecordMapper;
import assistant.mapper.QuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service

public class UserBasedRecommendService {

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Autowired
    private QuestionMapper questionMapper;
    /**
     * 推荐题目逻辑
     * @param targetUserId 目标用户ID
     * @param recommendCount 推荐数量
     * @param subjectId 当前科目ID（用于冷启动）
     */

    public List<Integer> recommendQuestions(Integer targetUserId, int recommendCount, Integer subjectId) {
        // 获取当前用户的评分向量
        List<PracticeRecord> targetRecords = practiceRecordMapper.selectList(
                new QueryWrapper<PracticeRecord>().eq("rec_user_id", targetUserId)
        );

        // 判断是否触发冷启动
        if (targetRecords.isEmpty()) {
            System.out.println("User " + targetUserId + " is in cold-start phase.");
            return recommendByPopularity(subjectId, recommendCount);
        }
        // 非冷启动状态：获取全表数据进行协同过滤计算
        List<PracticeRecord> allRecords = practiceRecordMapper.selectList(null);
        Map<Integer, Map<Integer, Double>> userScores = new HashMap<>();
        for (PracticeRecord rec : allRecords) {
            userScores.computeIfAbsent(rec.getRecUserId(), k -> new HashMap<>())
                    .put(rec.getRecQuesId(), rec.getRecIsCorrect() == 1 ? 5.0 : 1.0);
        }

        Map<Integer, Double> targetUserVector = userScores.get(targetUserId);

        // 协同过滤
        Map<Integer, Double> userSimilarities = new HashMap<>();
        for (Integer otherUserId : userScores.keySet()) {
            if (otherUserId.equals(targetUserId)) continue;
            double sim = calculatePearson(targetUserVector, userScores.get(otherUserId));
            userSimilarities.put(otherUserId, sim);
        }

        return userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(5)
                .flatMap(entry -> userScores.get(entry.getKey()).keySet().stream())
                .filter(quesId -> !targetUserVector.containsKey(quesId))
                .distinct()
                .limit(recommendCount)
                .collect(Collectors.toList());
    }

    private List<Integer> recommendByPopularity(Integer subjectId, int count) {
        // 先按热度查
        List<Integer> ids = practiceRecordMapper.selectHotQuestionIds(subjectId, count);

        // 如果全站都没人做过这个科目的题，则随机从题库抽题
        if (ids == null || ids.isEmpty()) {
            return questionMapper.selectRandomIdsBySubject(subjectId, count);
        }
        return ids;
    }



    private double calculatePearson(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        Set<Integer> commonKeys = new HashSet<>(v1.keySet());
        commonKeys.retainAll(v2.keySet());
        if (commonKeys.size() < 2) return 0.0; // 共同题目太少，相关性无意义

        double avg1 = v1.values().stream().mapToDouble(d -> d).average().orElse(0.0);
        double avg2 = v2.values().stream().mapToDouble(d -> d).average().orElse(0.0);

        double numerator = 0.0,sumSq1 = 0.0, sumSq2 = 0.0;

        for (Integer key : commonKeys) {
            double diff1 = v1.get(key) - avg1;
            double diff2 = v2.get(key) - avg2;

            numerator += diff1 * diff2;
            sumSq1 += diff1 * diff1;sumSq2 += diff2 * diff2;
        }

        if (sumSq1 == 0 || sumSq2 == 0) return 0.0;
        return numerator / (Math.sqrt(sumSq1) * Math.sqrt(sumSq2));
    }
}