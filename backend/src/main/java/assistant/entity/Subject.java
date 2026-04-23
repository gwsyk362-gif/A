package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("subjects")
public class Subject {
    @TableId(value = "sub_id", type = IdType.AUTO)
    private Integer subId;

    private String subName;

    private String subDescription;

    private LocalDateTime subCreateTime;
}