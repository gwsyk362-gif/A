package assistant.mapper;

import assistant.entity.VideoProgress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoProgressMapper extends BaseMapper<VideoProgress> {

    // 联合查询：获取用户未看完的视频详情和上次播放位置
    // 联合查询：获取用户未看完的视频详情、上次播放位置以及真实播放链接
    @Select("SELECT v.vid_id as vidId, v.vid_title as vidTitle, v.vid_cover_url as vidCoverUrl, " +
            "v.vid_url as vidUrl, v.vid_description as vidDescription, v.vid_duration as vidDuration, " +
            "p.prog_last_position as lastPosition, p.prog_update_time as lastWatchTime " +
            "FROM video_progress p " +
            "JOIN video_resources v ON p.prog_vid_id = v.vid_id " +
            "WHERE p.prog_user_id = #{userId} AND p.prog_is_finished = 0 " +
            "ORDER BY p.prog_update_time DESC")
    List<Map<String, Object>> getRecentWatchHistory(@Param("userId") Integer userId);
}