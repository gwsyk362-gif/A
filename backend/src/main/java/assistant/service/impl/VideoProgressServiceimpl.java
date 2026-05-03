package assistant.service.impl;

import assistant.entity.VideoProgress;
import assistant.mapper.VideoProgressMapper;
import assistant.service.VideoProgressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class VideoProgressServiceimpl extends ServiceImpl<VideoProgressMapper, VideoProgress> implements VideoProgressService {

    @Override
    public List<Map<String, Object>> getRecentWatchHistory(Integer userId) {
        return baseMapper.getRecentWatchHistory(userId);
    }

    // 新增：具体的保存/更新逻辑
    @Override
    public void saveOrUpdateProgress(VideoProgress progress) {
        // 先按 userId 和 vidId 查一下数据库里有没有记录
        QueryWrapper<VideoProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("prog_user_id", progress.getProgUserId())
                .eq("prog_vid_id", progress.getProgVidId());

        VideoProgress existing = this.getOne(queryWrapper);

        if (existing != null) {
            // 如果之前看过，更新播放时间和是否看完的状态
            existing.setProgLastPosition(progress.getProgLastPosition());
            existing.setProgIsFinished(progress.getProgIsFinished());
            this.updateById(existing);
        } else {
            // 如果是第一次看，直接插入新记录
            this.save(progress);
        }
    }
}