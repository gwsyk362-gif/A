package assistant.service;

import assistant.entity.PracticeRecord;
import assistant.entity.Question; // ✨ 记得导入 Question 实体
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
     */
    public List<Integer> recommendQuestions(Integer targetUserId, int recommendCount, Integer subjectId, String kp) {
        // ✨ 1. 如果前端传了具体的知识点，我们先获取该知识点下的所有题目 ID
        Set<Integer> validQuesIds = null;
        if (kp != null && !kp.trim().isEmpty()) {
            List<Question> questions = questionMapper.selectList(
                    new QueryWrapper<Question>().eq("ques_sub_id", subjectId).eq("ques_kp", kp)
            );
            validQuesIds = questions.stream().map(Question::getQuesId).collect(Collectors.toSet());

            // 如果该知识点下连一道题都没有，直接返回空
            if (validQuesIds.isEmpty()) {
                return new ArrayList<>();
            }
        }

        // 获取当前用户的评分向量
        List<PracticeRecord> targetRecords = practiceRecordMapper.selectList(
                new QueryWrapper<PracticeRecord>().eq("rec_user_id", targetUserId)
        );

        // 判断是否触发冷启动
        if (targetRecords.isEmpty()) {
            System.out.println("User " + targetUserId + " is in cold-start phase.");
            return recommendByPopularity(subjectId, recommendCount, validQuesIds);
        }

        // 非冷启动状态：获取全表数据进行协同过滤计算
        List<PracticeRecord> allRecords = practiceRecordMapper.selectList(null);
        Map<Integer, Map<Integer, Double>> userScores = new HashMap<>();
        for (PracticeRecord rec : allRecords) {
            userScores.computeIfAbsent(rec.getRecUserId(), k -> new HashMap<>())
                    .put(rec.getRecQuesId(), rec.getRecIsCorrect() == 1 ? 5.0 : 1.0);
        }

        Map<Integer, Double> targetUserVector = userScores.getOrDefault(targetUserId, new HashMap<>());

        // 协同过滤
        Map<Integer, Double> userSimilarities = new HashMap<>();
        for (Integer otherUserId : userScores.keySet()) {
            if (otherUserId.equals(targetUserId)) continue;
            double sim = calculatePearson(targetUserVector, userScores.get(otherUserId));
            userSimilarities.put(otherUserId, sim);
        }

        // 供 lambda 表达式使用
        final Set<Integer> finalValidQuesIds = validQuesIds;

        List<Integer> recommended = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(5)
                .flatMap(entry -> userScores.get(entry.getKey()).keySet().stream())
                .filter(quesId -> !targetUserVector.containsKey(quesId)) // 过滤掉自己已经做过的
                .filter(quesId -> finalValidQuesIds == null || finalValidQuesIds.contains(quesId)) // ✨ 核心：过滤知识点
                .distinct()
                .limit(recommendCount)
                .collect(Collectors.toList());

        //如果被知识点过滤完，用随机题目补齐
        if (recommended.size() < recommendCount) {
            List<Integer> padding = recommendByPopularity(subjectId, recommendCount - recommended.size(), finalValidQuesIds);
            for (Integer id : padding) {
                if (!recommended.contains(id) && !targetUserVector.containsKey(id)) {
                    recommended.add(id);
                    if (recommended.size() >= recommendCount) break;
                }
            }
        }

        return recommended;
    }

    //冷启动
    private List<Integer> recommendByPopularity(Integer subjectId, int count, Set<Integer> validQuesIds) {
        // 如果有知识点限制，原有针对全科目的“热度 SQL”就不适用了，直接从满足条件的 ID 里随机抽
        if (validQuesIds != null) {
            List<Integer> list = new ArrayList<>(validQuesIds);
            Collections.shuffle(list);
            return list.stream().limit(count).collect(Collectors.toList());
        }

        // 按热度查找（全科目）
        List<Integer> ids = practiceRecordMapper.selectHotQuestionIds(subjectId, count);

        // 没人做过 随机抽题
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
            sumSq1 += diff1 * diff1;
            sumSq2 += diff2 * diff2;
        }

        if (sumSq1 == 0 || sumSq2 == 0) return 0.0;
        return numerator / (Math.sqrt(sumSq1) * Math.sqrt(sumSq2));
    }
}