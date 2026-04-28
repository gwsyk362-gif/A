package assistant.service.impl;

import assistant.entity.PracticeRecord;
import assistant.mapper.PracticeRecordMapper;
import assistant.service.PracticeRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 做题记录服务实现类
 */
@Service
public class PracticeRecordServiceimpl extends ServiceImpl<PracticeRecordMapper, PracticeRecord> implements PracticeRecordService {
    // 同样，暂时不需要写任何代码。
    // ServiceImpl 会自动将 PracticeRecordMapper 注入进来，并实现 IService 中的所有方法
}