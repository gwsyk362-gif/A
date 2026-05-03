package assistant.controller;

import assistant.entity.VideoProgress;
import assistant.service.VideoProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/videoProgress")
public class VideoProgressController {

    @Autowired
    private VideoProgressService videoProgressService;

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentWatchHistory(@RequestParam Integer userId) {
        return ResponseEntity.ok(videoProgressService.getRecentWatchHistory(userId));
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveProgress(@RequestBody VideoProgress progress) {
        try {
            videoProgressService.saveOrUpdateProgress(progress);
            return ResponseEntity.ok("进度保存成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("进度保存失败: " + e.getMessage());
        }
    }
}