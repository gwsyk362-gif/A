package assistant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class UploadController {

    private final String uploadPath = "D:/JAVA/javap/A/videoCover/";

    @PostMapping("/cover")
    public ResponseEntity<String> uploadCover(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subjectName", defaultValue = "unknown") String subjectName
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("文件不能为空");
        }

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

        // 去除科目名称中可能导致 Windows 文件系统报错的特殊字符（以防万一）
        String safeSubjectName = subjectName.replaceAll("[\\\\/:*?\"<>|]", "_");
        //拼接新文件名：科目名__随机UUID.jpg (如：数据库__b7a1...8d2e.jpg)
        String fileName = safeSubjectName + "__" + UUID.randomUUID().toString() + suffix;

        try {
            file.transferTo(new File(dir, fileName));
            return ResponseEntity.ok("/videoCover/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件保存失败");
        }
    }
}