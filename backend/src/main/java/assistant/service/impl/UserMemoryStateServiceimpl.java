package assistant.service.impl;

import assistant.entity.UserMemoryState;
import assistant.mapper.UserMemoryStateMapper;
import assistant.service.UserMemoryStateService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserMemoryStateServiceimpl extends ServiceImpl<UserMemoryStateMapper, UserMemoryState> implements UserMemoryStateService {

    @Override
    public void updateOrInitSM2State(Integer userId, Integer quesId, Integer isCorrect) {
        // 1. 查询该用户对该题的记忆记录
        QueryWrapper<UserMemoryState> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("ques_id", quesId);
        UserMemoryState state = this.getOne(query);

        boolean isNew = false;
        if (state == null) {
            // 如果是首次做这道题，初始化状态
            state = new UserMemoryState();
            state.setUserId(userId);
            state.setQuesId(quesId);
            state.setRepetitions(0);
            state.setIntervalDays(0);
            state.setEasinessFactor(2.5); // SM-2 默认初始简易度为 2.5
            isNew = true;
        }

        // 2. 将系统的对错转化为 SM-2 的 Quality 评分
        // 正确算作 5 分(完美回忆)，错误算作 1 分(回想失败)
        int quality = (isCorrect != null && isCorrect == 1) ? 5 : 1;

        // 3. 计算连续答对次数 (Repetitions) 和 间隔天数 (Interval)
        if (quality >= 3) {
            // 回答正确
            if (state.getRepetitions() == 0) {
                state.setIntervalDays(1);
            } else if (state.getRepetitions() == 1) {
                state.setIntervalDays(6);
            } else {
                state.setIntervalDays((int) Math.round(state.getIntervalDays() * state.getEasinessFactor()));
            }
            state.setRepetitions(state.getRepetitions() + 1);
        } else {
            // 回答错误，打回原形
            state.setRepetitions(0);
            state.setIntervalDays(1);
        }

        // 4. 计算新的简易度因子 (Easiness Factor)
        // 经典 SM-2 公式: EF = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        double newEF = state.getEasinessFactor() + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));

        // EF 值最低不能低于 1.3
        if (newEF < 1.3) {
            newEF = 1.3;
        }
        state.setEasinessFactor(newEF);

        // 5. 设置下一次复习的时间
        state.setNextReviewTime(LocalDateTime.now().plusDays(state.getIntervalDays()));
        state.setUpdateTime(LocalDateTime.now());

        // 6. 保存或更新入库
        if (isNew) {
            this.save(state);
        } else {
            this.updateById(state);
        }
    }
}