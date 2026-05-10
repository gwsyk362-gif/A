package assistant.service.impl;

import assistant.entity.VideoResource;
import assistant.mapper.VideoResourceMapper;
import assistant.service.VideoResourceFind;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class VideoResourceFindimpl extends ServiceImpl<VideoResourceMapper, VideoResource> implements VideoResourceFind {

    @Autowired  // 如果 Mapper 没有注入，需要手动注入
    private VideoResourceMapper videoResourceMapper;

    @Override
    public List<VideoResource> searchVideos(String keyword) {
        LambdaQueryWrapper<VideoResource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(VideoResource::getVidTitle, keyword)
                    .or()
                    .like(VideoResource::getVidDescription, keyword);
        }
        return this.list(wrapper);
    }

    @Override
    public IPage<VideoResource> getManagePage(int page, int size, String sortField, String sortOrder) {
        // 参数白名单校验
        if (!"viewCount".equals(sortField) && !"vidTitle".equals(sortField)) {
            sortField = "vidId";
        }
        if (!"ASC".equalsIgnoreCase(sortOrder) && !"DESC".equalsIgnoreCase(sortOrder)) {
            sortOrder = "DESC";
        }

        Page<VideoResource> pageObj = new Page<>(page, size);
        return videoResourceMapper.selectManagePage(pageObj, sortField, sortOrder.toUpperCase());
    }
}