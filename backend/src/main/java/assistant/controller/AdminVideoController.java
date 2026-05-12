package assistant.controller;

import assistant.entity.VideoResource;
import assistant.service.VideoResourceFind;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/videos")
public class AdminVideoController {

    @Autowired
    private VideoResourceFind videoResourceFind;

    // ---------- 管理端列表 ----------
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getManageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "viewCount") String sortField,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam(required = false) String keyword
    ) {
        IPage<VideoResource> result = videoResourceFind.getManagePage(page, size, sortField, sortOrder, keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("records", result.getRecords());
        response.put("total", result.getTotal());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());
        return ResponseEntity.ok(response);
    }

    // ---------- 删除 ----------
    @DeleteMapping("/{vidId}")
    public ResponseEntity<String> deleteVideo(@PathVariable Integer vidId) {
        boolean removed = videoResourceFind.removeById(vidId);
        return removed ? ResponseEntity.ok("删除成功") : ResponseEntity.status(500).body("删除失败");
    }

    // ---------- 新增/编辑 ----------
    @PostMapping("/save")
    public ResponseEntity<String> saveVideo(@RequestBody VideoResource video) {
        boolean saved = videoResourceFind.saveOrUpdate(video);
        return saved ? ResponseEntity.ok("保存成功") : ResponseEntity.status(500).body("保存失败");
    }
}