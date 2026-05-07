package assistant.service;

import assistant.entity.PracticeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface PracticeRecordService extends IService<PracticeRecord> {
    // 保存做题记录并动态更新用户的科目能力分
    boolean saveRecordsAndUpdateScore(List<PracticeRecord> records);

    List<Map<String, Object>> getKnowledgeMastery(Integer userId);
    List<Map<String, Object>> getDailyStats(Integer userId);
    List<Map<String, Object>> getErrorRecords(Integer userId);
}