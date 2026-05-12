package assistant.service.impl;

import assistant.entity.FavoriteArticle;
import assistant.mapper.FavoriteArticleMapper;
import assistant.service.FavoriteArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FavoriteArticleServiceimpl extends ServiceImpl<FavoriteArticleMapper, FavoriteArticle> implements FavoriteArticleService {
}