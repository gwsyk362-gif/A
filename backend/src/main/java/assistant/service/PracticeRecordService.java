package assistant.service;

import assistant.entity.PracticeRecord;
import assistant.entity.SubjectCoverageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PracticeRecordService extends IService<PracticeRecord> {

    boolean saveRecordsAndUpdateScore(List<PracticeRecord> records);

    List<Map<String, Object>> getKnowledgeMastery(Integer userId);
    List<Map<String, Object>> getDailyStats(Integer userId);
    List<Map<String, Object>> getErrorRecords(Integer userId);
    List<SubjectCoverageDTO> getSubjectCoverage(Integer userId);
    List<Map<String, Object>> getYearlyStats(Integer userId);
    List<Map<String, Object>> getHistoryRecords(Integer userId);
}