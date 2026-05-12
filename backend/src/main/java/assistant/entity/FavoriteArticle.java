package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite_articles")
public class FavoriteArticle {
    @TableId(value = "fav_id", type = IdType.AUTO)
    private Integer favId;

    private Integer favUserId;
    private Integer favArticleId;
    private LocalDateTime favCreateTime;
}