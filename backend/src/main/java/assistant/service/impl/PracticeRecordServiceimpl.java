package assistant.service.impl;

import assistant.entity.PracticeRecord;
import assistant.mapper.PracticeRecordMapper;
import assistant.service.PracticeRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PracticeRecordServiceimpl extends ServiceImpl<PracticeRecordMapper, PracticeRecord> implements PracticeRecordService {

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
}