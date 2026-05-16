package assistant.service;

import assistant.entity.*; // 假设你新增了 UserMemoryState 实体
import assistant.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserBasedRecommendService {

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserSubjectScoreMapper userSubjectScoreMapper;

    // 新增：引入 SM-2 记忆状态 Mapper
    @Autowired
    private UserMemoryStateMapper userMemoryStateMapper;

    public List<Question> recommendQuestions(Integer targetUserId, int recommendCount, Integer subjectId, String kp, String keyword) {

        // 1. 根据科目和知识点查询候选题目
        QueryWrapper<Question> qw = new QueryWrapper<>();
        qw.eq("ques_sub_id", subjectId);
        if (kp != null && !kp.trim().isEmpty()) {
            qw.eq("ques_kp", kp);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like("ques_content", keyword);
        }
        List<Question> candidateQuestions = questionMapper.selectList(qw);

        if (candidateQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 准备用户数据与 Elo 能力分 (保持原有逻辑)
        UserSubjectScore uss = userSubjectScoreMapper.selectOne(
                new QueryWrapper<UserSubjectScore>().eq("user_id", targetUserId).eq("sub_id", subjectId)
        );
        double targetUserScore = (uss != null && uss.getEloScore() != null) ? uss.getEloScore() : 1500.0;

        // 获取全局答题记录
        List<PracticeRecord> allRecords = practiceRecordMapper.selectList(null);

        // ================== 新增：拉取该用户的所有 SM-2 记忆状态 ==================
        List<UserMemoryState> memoryStates = userMemoryStateMapper.selectList(
                new QueryWrapper<UserMemoryState>().eq("user_id", targetUserId)
        );
        // 转为 Map 实现 O(1) 查找，Key 为 QuesId
        Map<Integer, UserMemoryState> sm2StateMap = memoryStates.stream()
                .collect(Collectors.toMap(UserMemoryState::getQuesId, state -> state));
        // ========================================================================

        // 整理目标用户历史记录 (用于计算预期胜率，保持原逻辑)
        Map<Integer, PracticeRecord> targetUserHistory = new HashMap<>();
        for (PracticeRecord rec : allRecords) {
            if (rec.getRecUserId().equals(targetUserId)) {
                if (!targetUserHistory.containsKey(rec.getRecQuesId()) ||
                        rec.getRecTime().isAfter(targetUserHistory.get(rec.getRecQuesId()).getRecTime())) {
                    targetUserHistory.put(rec.getRecQuesId(), rec);
                }
            }
        }

        // 动态自适应 ZPD 期望胜率计算 (保持原有逻辑)
        double targetExpectedWinRate;
        if (targetUserHistory.isEmpty()) {
            targetExpectedWinRate = 0.65;
        } else {
            List<PracticeRecord> sortedRecords = targetUserHistory.values().stream()
                    .sorted((r1, r2) -> r2.getRecTime().compareTo(r1.getRecTime()))
                    .collect(Collectors.toList());
            int limit = Math.min(20, sortedRecords.size());
            long correctCount = sortedRecords.stream().limit(limit).filter(r -> r.getRecIsCorrect() == 1).count();
            double recentAccuracy = (double) correctCount / limit;
            targetExpectedWinRate = 0.75 - (recentAccuracy * 0.5);
            targetExpectedWinRate = Math.max(0.25, Math.min(0.80, targetExpectedWinRate));
        }

        // 预处理皮尔逊CF数据 (保持原有逻辑)
        Map<Integer, Double> userSimilarities = calculateAllUserSimilarities(targetUserId, allRecords);
        Map<Integer, List<PracticeRecord>> recsByQues = allRecords.stream()
                .collect(Collectors.groupingBy(PracticeRecord::getRecQuesId));
        Set<Integer> similarUsers = userSimilarities.entrySet().stream()
                .filter(e -> e.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toSet());

        // 3. 核心多路加权融合打分
        List<QuestionScoreVO> scoredQuestions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Question q : candidateQuestions) {
            double finalScore = 0.0;
            int quesId = q.getQuesId();
            double quesScore = q.getQuesScore();

            // --- 第一路：自适应 ZPD 难度匹配 (权重 40%) ---
            double expectedWinRate = 1.0 / (1.0 + Math.pow(10, (quesScore - targetUserScore) / 400.0));
            double zpdScore = 1.0 - Math.abs(expectedWinRate - targetExpectedWinRate);
            if (zpdScore < 0) zpdScore = 0;
            finalScore += 0.40 * zpdScore + Math.random() * 0.05;

            // --- 第二路：基于用户的协同过滤 CF (权重 30%) ---
            double cfScore = 0.0;
            double simSum = 0.0;
            List<PracticeRecord> quesRecords = recsByQues.getOrDefault(quesId, Collections.emptyList());

            for (PracticeRecord rec : quesRecords) {
                Integer recUserId = rec.getRecUserId();
                if (!recUserId.equals(targetUserId) && similarUsers.contains(recUserId)) {
                    Double sim = userSimilarities.get(recUserId);
                    double rating = rec.getRecIsCorrect() == 1 ? 1.0 : 0.0;
                    cfScore += sim * rating;
                    simSum += sim;
                }
            }
            if (simSum > 0) cfScore = cfScore / simSum;
            finalScore += 0.30 * cfScore;

            // --- 第三路：全新 SM-2 间隔重复机制 (权重 30%) ---
            double memoryScore = 0.0;
            UserMemoryState sm2State = sm2StateMap.get(quesId);

            if (sm2State != null) {
                // 已有复习状态的题目
                long hoursUntilReview = ChronoUnit.HOURS.between(now, sm2State.getNextReviewTime());

                if (hoursUntilReview <= 0) {
                    // 已经逾期，急需复习。逾期时间越长，分数越趋近于 1.0
                    // 使用 Sigmoid 变体：逾期时间越久，分数越高 (上限1.0)
                    double overdueHours = Math.abs((double) hoursUntilReview);
                    memoryScore = 0.5 + (0.5 * (overdueHours / (overdueHours + 24.0))); // 逾期24小时即达到0.75，无限久趋近1.0
                } else {
                    // 还没到复习时间，抑制推荐
                    // 距离复习时间越近，分数稍微高一点点；距离越远，分数越低（甚至给负分抑制）
                    memoryScore = 0.1 * Math.exp(-0.01 * hoursUntilReview);
                }
            } else if (targetUserHistory.containsKey(quesId)) {
                // 异常处理：有做题记录，但没有 SM-2 状态（可能是旧数据），给个基础复习分
                memoryScore = 0.4;
            } else {
                // 完全没做过的新题：由 ZPD 和 CF 主导，记忆分为 0
                memoryScore = 0.0;
            }
            finalScore += 0.30 * memoryScore;

            // 微小扰动项
            finalScore += (quesId % 100) * 0.0001;
            scoredQuestions.add(new QuestionScoreVO(q.getQuesId(), finalScore));
        }

        // 4. 倒序排列并返回指定的推荐数量 (保持原有逻辑)
        List<Integer> topIds = scoredQuestions.stream()
                .sorted(Comparator.comparingDouble(QuestionScoreVO::getFinalScore).reversed())
                .limit(recommendCount)
                .map(QuestionScoreVO::getQuesId)
                .collect(Collectors.toList());

        if (topIds.isEmpty()) return new ArrayList<>();

        List<Question> resultQuestions = questionMapper.selectBatchIds(topIds);
        resultQuestions.sort(Comparator.comparingInt(q -> topIds.indexOf(q.getQuesId())));
        return resultQuestions;
    }

    // 组装用户向量并遍历计算皮尔逊相似度
    private Map<Integer, Double> calculateAllUserSimilarities(Integer targetUserId, List<PracticeRecord> allRecords) {
        Map<Integer, Map<Integer, Double>> userScores = new HashMap<>();
        for (PracticeRecord rec : allRecords) {
            userScores.computeIfAbsent(rec.getRecUserId(), k -> new HashMap<>())
                    .put(rec.getRecQuesId(), rec.getRecIsCorrect() == 1 ? 5.0 : 1.0);
        }

        Map<Integer, Double> targetUserVector = userScores.getOrDefault(targetUserId, new HashMap<>());
        Map<Integer, Double> userSimilarities = new HashMap<>();

        for (Integer otherUserId : userScores.keySet()) {
            if (otherUserId.equals(targetUserId)) continue;
            double sim = calculatePearson(targetUserVector, userScores.get(otherUserId));
            userSimilarities.put(otherUserId, sim);
        }
        return userSimilarities;
    }

    // 皮尔逊相关系数算法实现
    private double calculatePearson(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        Set<Integer> commonKeys = new HashSet<>(v1.keySet());
        commonKeys.retainAll(v2.keySet());
        if (commonKeys.size() < 5) return 0.0;

        double avg1 = 0.0, avg2 = 0.0;
        for (int key : commonKeys) {
            avg1 += v1.get(key);
            avg2 += v2.get(key);
        }
        avg1 /= commonKeys.size();
        avg2 /= commonKeys.size();

        double numerator = 0.0, sumSq1 = 0.0, sumSq2 = 0.0;
        for (int key : commonKeys) {
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