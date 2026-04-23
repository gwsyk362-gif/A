package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("login_accounts")
public class LoginAccount {
    @TableId(value = "acc_id", type = IdType.AUTO)
    private Integer accId;

    private String accUsername;

    private String accPassword;

    private Integer accUserId;
}