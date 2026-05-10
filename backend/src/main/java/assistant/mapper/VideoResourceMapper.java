package assistant.mapper;

import assistant.entity.VideoResource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VideoResourceMapper extends BaseMapper<VideoResource> {

    /**
     * 管理端分页查询视频列表，并统计观看量
     * @param page      分页对象
     * @param sortField 排序字段（仅允许 vidTitle 或 viewCount）
     * @param sortOrder 排序方向（ASC / DESC）
     */
    @Select("<script>" +
            "SELECT v.*, COALESCE(p.view_count, 0) AS viewCount " +
            "FROM video_resources v " +
            "LEFT JOIN ( " +
            "   SELECT prog_vid_id, COUNT(*) AS view_count " +
            "   FROM video_progress " +
            "   GROUP BY prog_vid_id " +
            ") p ON v.vid_id = p.prog_vid_id " +
            "ORDER BY " +
            "   <choose>" +
            "       <when test='sortField == \"viewCount\"'> viewCount ${sortOrder} </when>" +
            "       <otherwise> v.vid_id ${sortOrder} </otherwise>" +
            "   </choose>" +
            "</script>")
    IPage<VideoResource> selectManagePage(Page<VideoResource> page,
                                          @Param("sortField") String sortField,
                                          @Param("sortOrder") String sortOrder);
}