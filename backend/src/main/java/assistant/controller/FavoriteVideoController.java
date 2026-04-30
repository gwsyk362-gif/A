package assistant.controller;

import assistant.entity.FavoriteVideo;
import assistant.entity.VideoResource;
import assistant.service.FavoriteVideoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favoriteVideos")
public class FavoriteVideoController {

    @Autowired
    private FavoriteVideoService favoriteVideoService;

    // 获取用户所有收藏的视频ID列表
    @GetMapping("/ids")
    public List<Integer> getFavoriteIds(@RequestParam Integer userId) {
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", userId);
        List<FavoriteVideo> list = favoriteVideoService.list(queryWrapper);

        return list.stream()
                .map(FavoriteVideo::getFavVidsId)
                .collect(Collectors.toList());
    }

    // 添加收藏
    @PostMapping("/add")
    public String addFavorite(@RequestBody FavoriteVideo favorite) {
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", favorite.getFavUserId())
                .eq("fav_vids_id", favorite.getFavVidsId());

        if (favoriteVideoService.count(queryWrapper) > 0) {
            return "already exists";
        }

        favorite.setFavCreateTime(LocalDateTime.now());
        boolean saved = favoriteVideoService.save(favorite);
        return saved ? "success" : "fail";
    }

    // 取消收藏
    @PostMapping("/remove")
    public String removeFavorite(@RequestBody FavoriteVideo favorite) {
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", favorite.getFavUserId())
                .eq("fav_vids_id", favorite.getFavVidsId());

        boolean removed = favoriteVideoService.remove(queryWrapper);
        return removed ? "success" : "fail";
    }

    // 获取收藏夹详情
    @GetMapping("/details")
    public List<VideoResource> getFavoriteVideoDetails(@RequestParam Integer userId) {
        // 直接调用 Service 层的 SQL 查询
        return favoriteVideoService.getFavoriteVideoDetails(userId);
    }
}