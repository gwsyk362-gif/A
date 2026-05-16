package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_memory_states")
public class UserMemoryState {
    @TableId(value = "um_id", type = IdType.AUTO)
    private Long umId;

    private Integer userId;
    private Integer quesId;

    // SM-2 核心状态字段
    private Integer repetitions;       // 连续答对次数
    private Integer intervalDays;      // 当前复习间隔(天)
    private Double easinessFactor;     // 简易度因子(EF)
    private LocalDateTime nextReviewTime; // 下次建议复习时间

    private LocalDateTime updateTime;
}