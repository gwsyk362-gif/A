package assistant.service;

import assistant.entity.PracticeRecord;
import assistant.entity.Question;
import assistant.entity.QuestionScoreVO;
import assistant.entity.UserSubjectScore;
import assistant.mapper.PracticeRecordMapper;
import assistant.mapper.QuestionMapper;
import assistant.mapper.UserSubjectScoreMapper;
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


    public List<Question> recommendQuestions(Integer targetUserId, int recommendCount, Integer subjectId, String kp, String keyword) {

        System.out.println(">>> 推荐方法被触发了！");
        // 1. 根据科目和知识点查所有题目
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

        // 2. 准备用户数据
        // 获取用户当前的科目能力分
        UserSubjectScore uss = userSubjectScoreMapper.selectOne(
                new QueryWrapper<UserSubjectScore>().eq("user_id", targetUserId).eq("sub_id", subjectId)
        );
        double targetUserScore = (uss != null && uss.getEloScore() != null) ? uss.getEloScore() : 1500.0;

        //获取全局答题记录用于计算 CF 和历史情况
        List<PracticeRecord> allRecords = practiceRecordMapper.selectList(null);

        //整理用户的答题记录 ，用于计算遗忘曲线
        Map<Integer, PracticeRecord> targetUserHistory = new HashMap<>();
        for (PracticeRecord rec : allRecords) {
            if (rec.getRecUserId().equals(targetUserId)) {
                // 如果是新记录或者时间更晚的记录，则覆盖
                if (!targetUserHistory.containsKey(rec.getRecQuesId()) ||
                        rec.getRecTime().isAfter(targetUserHistory.get(rec.getRecQuesId()).getRecTime())) {
                    targetUserHistory.put(rec.getRecQuesId(), rec);
                }
            }
        }

        // 用户做题表为空 冷启动机制
        if (targetUserHistory.isEmpty()) {
            System.out.println("用户 " + targetUserId + " 处于冷启动阶段，启动兜底推荐策略。");
            Set<Integer> validQuesIds = null;
            if (kp != null && !kp.trim().isEmpty()) {
                validQuesIds = candidateQuestions.stream().map(Question::getQuesId).collect(Collectors.toSet());
            }
            return recommendByPopularity(subjectId, recommendCount, validQuesIds);
        }

        //计算Pearson 相似度
        Map<Integer, Double> userSimilarities = calculateAllUserSimilarities(targetUserId, allRecords);


        //3. 排序
        List<QuestionScoreVO> scoredQuestions = new ArrayList<>();

        for (Question q : candidateQuestions) {
            double finalScore = 0.0;
            int quesId = q.getQuesId();
            double quesScore = q.getQuesScore(); // 题目难度分

            // ZPD 40
            double expectedWinRate = 1.0 / (1.0 + Math.pow(10, (quesScore - targetUserScore) / 400.0));
            // 假设 0.55 是最佳挑战胜率 计算偏差
            double zpdScore = 1.0 - Math.abs(expectedWinRate - 0.45);;
            if (zpdScore < 0) zpdScore = 0;
            finalScore += 0.40 * zpdScore + Math.random() * 0.05;


            // 协同过滤 30
            double cfScore = 0.0;
            double simSum = 0.0;
            for (PracticeRecord rec : allRecords) {
                if (rec.getRecQuesId().equals(quesId) && !rec.getRecUserId().equals(targetUserId)) {
                    Double sim = userSimilarities.getOrDefault(rec.getRecUserId(), 0.0);
                    if (sim > 0) {
                        double rating = rec.getRecIsCorrect() == 1 ? 1.0 : 0.0; // 做对算1分，做错算0分
                        cfScore += sim * rating;
                        simSum += sim;
                    }
                }
            }
            if (simSum > 0) cfScore = cfScore / simSum;
            finalScore += 0.30 * cfScore;


            //遗忘曲线 30
            double memoryScore = 0.5; // 对于没做过的新题，给一个中等的复习得分
            PracticeRecord lastRecord = targetUserHistory.get(quesId);

            if (lastRecord != null) {
                if (lastRecord.getRecIsCorrect() == 0) {
                    // 如果是历史错题：计算距今过去的小时数
                    long hoursBetween = ChronoUnit.HOURS.between(lastRecord.getRecTime(), LocalDateTime.now());
                    if(hoursBetween < 0) hoursBetween = 0;
                    // 艾宾浩斯指数衰减公式 (0.05 是衰减系数，可微调)
                    double retentionRate = Math.exp(-0.05 * hoursBetween);
                    // 遗忘得越多 (保留率越低)，越急需复习，得分越高
                    memoryScore = 1.0 - retentionRate;
                } else {
                    memoryScore = 0.1;
                }
            }
            finalScore += 0.30 * memoryScore;

            finalScore += (quesId % 100) * 0.0001;
            scoredQuestions.add(new QuestionScoreVO(q.getQuesId(), finalScore));
        }

        // 倒序排列并返回 ID
        List<Integer> topIds = scoredQuestions.stream()
                .sorted(Comparator.comparingDouble(QuestionScoreVO::getFinalScore).reversed())
                .limit(recommendCount)
                .map(QuestionScoreVO::getQuesId)
                .collect(Collectors.toList());

        if (topIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取实体对象并排序
        List<Question> resultQuestions = questionMapper.selectBatchIds(topIds);

        resultQuestions.sort(Comparator.comparingInt(q -> topIds.indexOf(q.getQuesId())));
        return resultQuestions;
    }

    //冷启动
    private List<Question> recommendByPopularity(Integer subjectId, int count, Set<Integer> validQuesIds) {
        List<Integer> ids;
        if (validQuesIds != null) {
            ids = new ArrayList<>(validQuesIds);
            Collections.shuffle(ids);
            ids = ids.stream().limit(count).collect(Collectors.toList());
        } else {
            ids = questionMapper.selectHotQuestionIds(subjectId, count);
            if (ids == null || ids.isEmpty()) {
                ids = questionMapper.selectRandomIdsBySubject(subjectId, count);
            }
        }
        if (ids.isEmpty()) return new ArrayList<>();

        List<Question> questions = questionMapper.selectBatchIds(ids);
        List<Integer> finalIds = ids;
        questions.sort(Comparator.comparingInt(q -> finalIds.indexOf(q.getQuesId())));
        return questions;
    }

    //pearson相关系数
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

    //pearson系数
    private double calculatePearson(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        Set<Integer> commonKeys = new HashSet<>(v1.keySet());
        commonKeys.retainAll(v2.keySet());
        if (commonKeys.size() < 2) return 0.0;

        double avg1 = v1.values().stream().mapToDouble(d -> d).average().orElse(0.0);
        double avg2 = v2.values().stream().mapToDouble(d -> d).average().orElse(0.0);

        double numerator = 0.0, sumSq1 = 0.0, sumSq2 = 0.0;

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