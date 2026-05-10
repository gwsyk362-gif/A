package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Accessors(chain = true)
@TableName("login_logs")
public class LoginLog implements Serializable {

    @TableId(value = "login_id", type = IdType.AUTO)
    private Long loginId;
    private Integer userId;
    private LocalDateTime loginTime;
    private LocalDate loginDate;
}