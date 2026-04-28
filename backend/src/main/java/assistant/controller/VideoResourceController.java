package assistant.controller;

import assistant.entity.VideoResource;
import assistant.service.VideoResourceFind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoResourceController {

    @Autowired
    private VideoResourceFind videoResourceFind;

    @GetMapping("/list")
    public ResponseEntity<List<VideoResource>> getVideos(@RequestParam(required = false) String keyword) {
        List<VideoResource> list = videoResourceFind.searchVideos(keyword);

        return ResponseEntity.ok(list);
    }
}