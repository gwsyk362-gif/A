package assistant.controller;

import assistant.entity.FavoriteQuestion;
import assistant.entity.Question;
import assistant.service.FavoriteQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favoriteQuestions")
public class FavoriteQuestionController {

    @Autowired
    private FavoriteQuestionService favoriteQuestionService;

    // 获取用户所有收藏的题目ID列表（用于前端界面初始化小星星状态）
    @GetMapping("/ids")
    public List<Integer> getFavoriteIds(@RequestParam Integer userId) {
        QueryWrapper<FavoriteQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", userId);
        List<FavoriteQuestion> list = favoriteQuestionService.list(queryWrapper);

        return list.stream().map(FavoriteQuestion::getFavQuesId).collect(Collectors.toList());
    }

    // 添加收藏
    @PostMapping("/add")
    public String addFavorite(@RequestBody FavoriteQuestion favorite) {
        QueryWrapper<FavoriteQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", favorite.getFavUserId())
                .eq("fav_ques_id", favorite.getFavQuesId());
        if (favoriteQuestionService.count(queryWrapper) > 0) {
            return "already exists";
        }

        favorite.setFavCreateTime(LocalDateTime.now());
        boolean saved = favoriteQuestionService.save(favorite);
        return saved ? "success" : "fail";
    }

    // 取消收藏
    @PostMapping("/remove")
    public String removeFavorite(@RequestBody FavoriteQuestion favorite) {
        QueryWrapper<FavoriteQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fav_user_id", favorite.getFavUserId())
                .eq("fav_ques_id", favorite.getFavQuesId());
        boolean removed = favoriteQuestionService.remove(queryWrapper);
        return removed ? "success" : "fail";
    }

    @GetMapping("/details")
    public List<Question> getFavoriteQuestionDetails(@RequestParam Integer userId) {
        // 直接调用 Service 层封装好的方法
        return favoriteQuestionService.getFavoriteQuestionDetails(userId);
    }
}