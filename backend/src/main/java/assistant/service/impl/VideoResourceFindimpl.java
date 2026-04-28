package assistant.service.impl;

import assistant.entity.VideoResource;
import assistant.mapper.VideoResourceMapper;
import assistant.service.VideoResourceFind;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class VideoResourceFindimpl extends ServiceImpl<VideoResourceMapper, VideoResource> implements VideoResourceFind {

    @Override
    public List<VideoResource> searchVideos(String keyword) {
        LambdaQueryWrapper<VideoResource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 查找视频
            wrapper.like(VideoResource::getVidTitle, keyword)
                    .or()
                    .like(VideoResource::getVidDescription, keyword);
        }
        return this.list(wrapper);
    }
}