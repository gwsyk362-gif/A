package assistant.mapper;

import assistant.entity.FavoriteVideo;
import assistant.entity.VideoResource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteVideoMapper extends BaseMapper<FavoriteVideo> {

    @Select("SELECT v.* FROM video_resources v " +
            "JOIN favorites_videos fv ON v.vid_id = fv.fav_vids_id " +
            "WHERE fv.fav_user_id = #{userId}")
    List<VideoResource> getFavoriteVideoDetails(Integer userId);

}