package assistant.service;

import assistant.entity.FavoriteVideo;
import assistant.entity.VideoResource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface FavoriteVideoService extends IService<FavoriteVideo> {

    List<VideoResource> getFavoriteVideoDetails(Integer userId);
}