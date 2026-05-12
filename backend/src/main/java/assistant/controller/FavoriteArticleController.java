package assistant.controller;

import assistant.entity.FavoriteArticle;
import assistant.entity.TextResourceVO;
import assistant.service.FavoriteArticleService;
import assistant.service.TextResourceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favoriteArticles")
@CrossOrigin
public class FavoriteArticleController {

    @Autowired
    private FavoriteArticleService favoriteArticleService;

    @Autowired
    private TextResourceService textResourceService;

    // 1. 添加收藏 (增加校验逻辑)
    @PostMapping("/add")
    public String addFavorite(@RequestBody FavoriteArticle favorite) {
        // 校验：文章是否存在
        if (textResourceService.getById(favorite.getFavArticleId()) == null) {
            return "article not found";
        }

        // 校验：是否已收藏
        QueryWrapper<FavoriteArticle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", favorite.getFavUserId())
                .eq("fav_article_id", favorite.getFavArticleId());

        if (favoriteArticleService.getOne(queryWrapper) != null) {
            return "already exists";
        }

        // 插入收藏记录
        favorite.setFavCreateTime(LocalDateTime.now());
        boolean success = favoriteArticleService.save(favorite);
        return success ? "success" : "fail";
    }

    // 2. 取消收藏
    @PostMapping("/remove")
    public String removeFavorite(@RequestBody FavoriteArticle favorite) {
        QueryWrapper<FavoriteArticle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", favorite.getFavUserId())
                .eq("fav_article_id", favorite.getFavArticleId());

        boolean success = favoriteArticleService.remove(queryWrapper);
        return success ? "success" : "fail";
    }

    // 3. 获取用户收藏的所有文章ID
    @GetMapping("/ids")
    public List<Integer> getFavoriteArticleIds(@RequestParam Integer userId) {
        QueryWrapper<FavoriteArticle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", userId);

        List<FavoriteArticle> favorites = favoriteArticleService.list(queryWrapper);
        return favorites.stream()
                .map(FavoriteArticle::getFavArticleId)
                .collect(Collectors.toList());
    }

}