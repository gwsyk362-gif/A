package assistant.controller;

import assistant.entity.ReadingHistory;
import assistant.service.ReadingHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/history")
@CrossOrigin
public class ReadingHistoryController {

    @Autowired
    private ReadingHistoryService historyService;

    // 记录一次观看
    @PostMapping("/record")
    public boolean record(@RequestBody ReadingHistory history) {
        // 强制设置当前时间为观看时间，防止客户端传入虚假时间
        history.setViewTime(LocalDateTime.now());
        return historyService.save(history);
    }

    // 删除单条历史记录
    @DeleteMapping("/{historyId}")
    public boolean delete(@PathVariable Long historyId) {
        return historyService.removeById(historyId);
    }

    // 分页查询某个用户的观看历史（按时间倒序）
    @GetMapping("/userPage")
    public Page<ReadingHistory> getUserHistoryPage(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<ReadingHistory> page = new Page<>(current, size);
        QueryWrapper<ReadingHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("view_time");
        return historyService.page(page, wrapper);
    }

    // 查询某篇文章的所有观看记录（按时间倒序，可扩展分页）
    @GetMapping("/article")
    public Page<ReadingHistory> getArticleHistoryPage(
            @RequestParam Integer articleId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<ReadingHistory> page = new Page<>(current, size);
        QueryWrapper<ReadingHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId)
                .orderByDesc("view_time");
        return historyService.page(page, wrapper);
    }


}