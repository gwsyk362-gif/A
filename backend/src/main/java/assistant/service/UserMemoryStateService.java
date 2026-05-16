package assistant.service;

import assistant.entity.UserMemoryState;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserMemoryStateService extends IService<UserMemoryState> {

    /**
     * 核心方法：根据用户的作答对错，更新或初始化 SM-2 记忆状态
     * @param userId 用户ID
     * @param quesId 题目ID
     * @param isCorrect 是否正确 (1正确, 0错误)
     */
    void updateOrInitSM2State(Integer userId, Integer quesId, Integer isCorrect);
}