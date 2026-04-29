package assistant.service.impl;

import assistant.entity.FavoriteVideo;
import assistant.entity.VideoResource;
import assistant.mapper.FavoriteVideoMapper;
import assistant.service.FavoriteVideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 修正点：删除所有与 Question 相关的导入和注入
 */
@Service
public class FavoriteVideoServiceimpl extends ServiceImpl<FavoriteVideoMapper, FavoriteVideo> implements FavoriteVideoService {

    @Autowired
    private FavoriteVideoMapper favoriteVideoMapper;

    @Override
    public List<VideoResource> getFavoriteVideoDetails(Integer userId) {
        // 确保你的 FavoriteVideoMapper 中确实定义了此 SQL 方法
        return favoriteVideoMapper.getFavoriteVideoDetails(userId);
    }
}