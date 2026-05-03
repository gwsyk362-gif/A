package assistant.service;

import assistant.entity.VideoProgress;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface VideoProgressService extends IService<VideoProgress> {
    List<Map<String, Object>> getRecentWatchHistory(Integer userId);

    // 新增：保存或更新视频播放进度
    void saveOrUpdateProgress(VideoProgress progress);
}