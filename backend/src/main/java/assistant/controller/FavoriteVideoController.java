package assistant.controller;

import assistant.entity.FavoriteVideo;
import assistant.entity.VideoResource;
import assistant.service.FavoriteVideoService;
import assistant.service.VideoResourceFind;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favoriteVideos") // 建议改为视频相关的路径
public class FavoriteVideoController {

    @Autowired
    private FavoriteVideoService favoriteVideoService;

    @Autowired
    private VideoResourceFind.VideoResourceService videoResourceService;

    // 获取用户所有收藏的视频ID列表
    @GetMapping("/ids")
    public List<Integer> getFavoriteIds(@RequestParam Integer userId) {
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        // 注意：这里使用数据库列名 fv_user_id
        queryWrapper.eq("fv_user_id", userId);
        List<FavoriteVideo> list = favoriteVideoService.list(queryWrapper);

        return list.stream()
                .map(FavoriteVideo::getFvVidId) // 修正了Getter方法名
                .collect(Collectors.toList());
    }

    // 添加收藏
    @PostMapping("/add")
    public String addFavorite(@RequestBody FavoriteVideo favorite) {
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fv_user_id", favorite.getFvUserId())
                .eq("fv_vid_id", favorite.getFvVidId());

        if (favoriteVideoService.count(queryWrapper) > 0) {
            return "already exists";
        }

        favorite.setFvCreateTime(LocalDateTime.now());
        boolean saved = favoriteVideoService.save(favorite);
        return saved ? "success" : "fail";
    }

    // 取消收藏
    @PostMapping("/remove")
    public String removeFavorite(@RequestBody FavoriteVideo favorite) {
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fv_user_id", favorite.getFvUserId())
                .eq("fv_vid_id", favorite.getFvVidId());

        boolean removed = favoriteVideoService.remove(queryWrapper);
        return removed ? "success" : "fail";
    }

    // 获取收藏夹详情
    @GetMapping("/details")
    public List<VideoResource> getFavoriteVideoDetails(@RequestParam Integer userId) {

        return favoriteVideoService.getFavoriteVideoDetails(userId);
    }
}