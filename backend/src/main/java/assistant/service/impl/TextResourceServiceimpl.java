package assistant.service.impl;

import assistant.entity.TextResource;
import assistant.entity.TextResourceVO;
import assistant.mapper.TextResourceMapper;
import assistant.service.TextResourceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TextResourceServiceimpl extends ServiceImpl<TextResourceMapper, TextResource> implements TextResourceService {
    @Override
    public IPage<TextResourceVO> getArticlePage(Page<TextResourceVO> page, String keyword) {
        return baseMapper.selectArticlePage(page, keyword);
    }

    @Override
    public TextResourceVO getArticleDetailVO(Integer articleId) {
        return baseMapper.selectArticleDetailVO(articleId);
    }
}