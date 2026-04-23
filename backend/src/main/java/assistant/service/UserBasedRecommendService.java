package assistant.service;

import assistant.entity.PracticeRecord;
import assistant.mapper.PracticeRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserBasedRecommendService {

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    public List<Integer> recommendQuestions(Integer targetUserId, int recommendCount) {
        List<PracticeRecord> allRecords = practiceRecordMapper.selectList(null);
        System.out.println("Found " + allRecords.size() + " practice records");

        Map<Integer, Map<Integer, Double>> userScores = new HashMap<>();
        for (PracticeRecord rec : allRecords) {
            userScores.computeIfAbsent(rec.getRecUserId(), k -> new HashMap<>())
                    .put(rec.getRecQuesId(), rec.getRecIsCorrect() == 1 ? 5.0 : 1.0);
        }
        System.out.println("Built user scores for " + userScores.size() + " users");

        Map<Integer, Double> targetUserVector = userScores.getOrDefault(targetUserId, new HashMap<>());
        if (targetUserVector.isEmpty()) {
            System.out.println("No records found for user " + targetUserId);
            return new ArrayList<>();
        }


        Map<Integer, Double> userSimilarities = new HashMap<>();
        for (Integer otherUserId : userScores.keySet()) {
            if (otherUserId.equals(targetUserId)) continue;
            double sim = calculateCosine(targetUserVector, userScores.get(otherUserId));
            userSimilarities.put(otherUserId, sim);
        }
        System.out.println("Calculated similarities for " + userSimilarities.size() + " users");


        return userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(5)
                .flatMap(entry -> userScores.get(entry.getKey()).keySet().stream())
                .filter(quesId -> !targetUserVector.containsKey(quesId))
                .distinct()
                .limit(recommendCount)
                .collect(Collectors.toList());
    }

    private double calculateCosine(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        Set<Integer> commonKeys = new HashSet<>(v1.keySet());
        commonKeys.retainAll(v2.keySet());

        if (commonKeys.isEmpty()) return 0.0;

        double dotProduct = 0.0;
        for (Integer key : commonKeys) dotProduct += v1.get(key) * v2.get(key);

        double norm1 = v1.values().stream().mapToDouble(d -> d * d).sum();
        double norm2 = v2.values().stream().mapToDouble(d -> d * d).sum();

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
