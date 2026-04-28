package assistant.service;

import assistant.entity.VideoResource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VideoResourceFind {
    List<VideoResource> searchVideos(String keyword);

    public interface VideoResourceService extends IService<VideoResource> {

        // 根据关键字模糊查询
        List<VideoResource> searchVideos(String keyword);
    }
}
