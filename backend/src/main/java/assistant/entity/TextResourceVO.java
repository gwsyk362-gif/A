package assistant.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextResourceVO extends TextResource {
    private String authorName;   // 作者昵称
    private Integer favoriteCount; // 收藏总数
}