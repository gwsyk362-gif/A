package assistant.controller;

import assistant.entity.PracticeRecord;
import assistant.service.PracticeRecordService; // 确保你有这个 Service 接口和实现类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/records")
public class PracticeRecordController {

    @Autowired
    private PracticeRecordService practiceRecordService;

    @PostMapping("/submitBatch")
    public ResponseEntity<?> submitBatchRecords(@RequestBody List<PracticeRecord> records) {
        // 为每条记录设置当前时间
        LocalDateTime now = LocalDateTime.now();
        records.forEach(record -> record.setRecTime(now));

        boolean success = practiceRecordService.saveBatch(records);

        if (success) {
            return ResponseEntity.ok("交卷成功，记录已保存");
        } else {
            return ResponseEntity.status(500).body("保存记录失败");
        }
    }

    @GetMapping("/stats/knowledge")
    public ResponseEntity<?> getKnowledgeMastery(@RequestParam Integer userId) {
        return ResponseEntity.ok(practiceRecordService.getKnowledgeMastery(userId));
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<?> getDailyStats(@RequestParam Integer userId) {
        return ResponseEntity.ok(practiceRecordService.getDailyStats(userId));
    }

    @GetMapping("/errors")
    public ResponseEntity<?> getErrorRecords(@RequestParam Integer userId) {
        return ResponseEntity.ok(practiceRecordService.getErrorRecords(userId));
    }
}