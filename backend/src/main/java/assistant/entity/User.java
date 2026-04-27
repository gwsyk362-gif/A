package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users")
public class User {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private Long username;  //八位
    private String password;
    private String nickname;
}