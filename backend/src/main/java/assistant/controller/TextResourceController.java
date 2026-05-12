package assistant.controller;

import assistant.entity.TextResource;
import assistant.entity.TextResourceVO;
import assistant.service.TextResourceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin
public class TextResourceController {

    @Autowired
    private TextResourceService textResourceService;

    // 1. 分页搜索
    @GetMapping("/page")
    public IPage<TextResourceVO> getArticlePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<TextResourceVO> page = new Page<>(current, size);
        return textResourceService.getArticlePage(page, keyword);
    }

    // 2. 获取文章详情 (返回VO，同时阅读量 +1)
    @GetMapping("/{id}")
    public TextResourceVO getArticleDetail(@PathVariable Integer id) {
        // 使用新方法获取包含 authorName 的完整数据
        TextResourceVO articleVO = textResourceService.getArticleDetailVO(id);

        if (articleVO != null) {
            int currentViews = articleVO.getViewCount() != null ? articleVO.getViewCount() : 0;

            // 仅实例化一个包含 ID 和需要更新字段的对象，避免并发覆盖其他数据
            TextResource updateEntity = new TextResource();
            updateEntity.setArticleId(id);
            updateEntity.setViewCount(currentViews + 1);

            textResourceService.updateById(updateEntity);

            // 将最新的阅读量设置回 VO 返回给前端
            articleVO.setViewCount(currentViews + 1);
        }
        return articleVO;
    }

    // 3. 发布新文章
    @PostMapping("/add")
    public boolean addArticle(@RequestBody TextResource article) {
        return textResourceService.save(article);
    }

    // 4. 更新文章
    @PutMapping("/{id}")
    public boolean updateArticle(@PathVariable Integer id, @RequestBody TextResource article) {
        article.setArticleId(id);
        return textResourceService.updateById(article);
    }

    // 5. 删除文章
    @DeleteMapping("/{id}")
    public boolean deleteArticle(@PathVariable Integer id) {
        return textResourceService.removeById(id);
    }
}