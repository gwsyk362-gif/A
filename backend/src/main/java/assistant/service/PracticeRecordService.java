package assistant.service;

import assistant.entity.PracticeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface PracticeRecordService extends IService<PracticeRecord> {
    List<Map<String, Object>> getKnowledgeMastery(Integer userId);
    List<Map<String, Object>> getDailyStats(Integer userId);
    List<Map<String, Object>> getErrorRecords(Integer userId);
}