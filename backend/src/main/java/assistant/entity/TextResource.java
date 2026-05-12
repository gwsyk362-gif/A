package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("text_resources")
public class TextResource {
    @TableId(value = "article_id", type = IdType.AUTO)
    private Integer articleId;

    private String title;
    private String coverUrl;
    private String summary;
    private String content;
    private String contentType;
    private Integer authorId;
    private Integer viewCount;
    private LocalDateTime createTime;
}