package assistant.service;

import assistant.entity.VideoResource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VideoResourceFind extends IService<VideoResource> {

    // 根据关键字模糊查询
    List<VideoResource> searchVideos(String keyword);

    // 管理端分页查询（包含观看量，支持排序）
    com.baomidou.mybatisplus.core.metadata.IPage<VideoResource> getManagePage(int page, int size, String sortField, String sortOrder, String keyword);
}