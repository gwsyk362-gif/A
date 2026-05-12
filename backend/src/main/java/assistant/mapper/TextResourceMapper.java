package assistant.mapper;

import assistant.entity.TextResource;
import assistant.entity.TextResourceVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TextResourceMapper extends BaseMapper<TextResource> {

    // 使用注解形式编写关联查询 SQL
    @Select("<script>" +
            "SELECT r.*, u.nickname as authorName, " +
            "(SELECT COUNT(*) FROM favorite_articles f WHERE f.fav_article_id = r.article_id) as favoriteCount " +
            "FROM text_resources r " +
            "LEFT JOIN users u ON r.author_id = u.user_id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (r.title LIKE CONCAT('%',#{keyword},'%') OR r.summary LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "ORDER BY r.create_time DESC" +
            "</script>")
    IPage<TextResourceVO> selectArticlePage(Page<TextResourceVO> page, @Param("keyword") String keyword);

    // 根据 ID 查询单篇文章及其关联的作者信息
    @Select("SELECT t.*, u.nickname AS authorName " +
            "FROM text_resources t " +
            "LEFT JOIN users u ON t.author_id = u.user_id " +
            "WHERE t.article_id = #{articleId}")
    TextResourceVO selectArticleDetailVO(@Param("articleId") Integer articleId);

}