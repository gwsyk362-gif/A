package assistant.service;

import assistant.entity.PracticeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 做题记录服务接口
 */
public interface PracticeRecordService extends IService<PracticeRecord> {
    // 这里暂时不需要写任何代码，IService 已经包含了 save, saveBatch, list 等基础方法
    // 如果以后有复杂的自定义查询（比如：查询某个用户的错题本），可以在这里定义方法名
}