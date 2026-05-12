package assistant.service;

import assistant.entity.TextResource;
import assistant.entity.TextResourceVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TextResourceService extends IService<TextResource> {

    IPage<TextResourceVO> getArticlePage(Page<TextResourceVO> page, String keyword);

    TextResourceVO getArticleDetailVO(Integer articleId);
}