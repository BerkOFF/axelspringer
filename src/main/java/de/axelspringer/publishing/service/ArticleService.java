package de.axelspringer.publishing.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.axelspringer.publishing.model.Article;
import de.axelspringer.publishing.model.Article_;
import de.axelspringer.publishing.model.Author;
import de.axelspringer.publishing.model.Author_;
import de.axelspringer.publishing.persistence.ArticleRepository;
import de.axelspringer.publishing.persistence.AuthorRepository;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.ws.rs.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    EntityManager entityManager;

    public Article create(Article article) {
        article.setAuthors(resolveAuthors(article.getAuthors()));
        return articleRepository.saveAndFlush(article);
    }

    public Article read(Integer id) {
        ensureArticleExists(id);
        return articleRepository.getOne(id);
    }

    public Article update(Article article) {
        ensureArticleExists(article.getId());
        article.setAuthors(resolveAuthors(article.getAuthors()));
        Article origin = articleRepository.getOne(article.getId());
        try {
            BeanUtilsBean.getInstance().copyProperties(origin, article);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return articleRepository.saveAndFlush(origin);
    }

    public void delete(Integer id) {
        ensureArticleExists(id);
        articleRepository.delete(id);
    }

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    public List<Article> find(List<String> keywords, List<String> authors, OffsetDateTime publishedSince, OffsetDateTime publishedBefore) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Article> criteria = criteriaBuilder.createQuery(Article.class);
        Root<Article> articleRoot = criteria.from(Article.class);
        List<Predicate> predicates = Lists.newArrayList();

        if(keywords != null && !keywords.isEmpty()) {
            SetJoin<Article, String> keywordsJoin = articleRoot.join(Article_.keywords);
            predicates.add(keywordsJoin.in(keywords));
        }

        if(authors!=null && !authors.isEmpty()) {
            SetJoin<Article, Author> authorsJoin = articleRoot.join(Article_.authors);
            predicates.add(authorsJoin.get(Author_.name).in(authors));
        }

        if(publishedSince != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(articleRoot.get(Article_.published), publishedSince));
        }

        if(publishedBefore != null) {
            predicates.add(criteriaBuilder.lessThan(articleRoot.get(Article_.published), publishedBefore));
        }

        return entityManager.createQuery(criteria.where(predicates.toArray(new Predicate[predicates.size()])).distinct(true)).getResultList();
    }

    private void ensureArticleExists(Integer id) {
        if(!articleRepository.exists(id)) {
            throw new NotFoundException("Article doesn't exist");
        }
    }

    private Set<Author> resolveAuthors(Set<Author> authors) {
        Set<Author> resolvedAuthors = Sets.newHashSet();
        if (authors != null) {
            authors.removeIf(author -> {
                Author existing = authorRepository.find(author.getName());
                if (existing != null) {
                    resolvedAuthors.add(existing);
                    return true;
                }
                return false;
            });
            resolvedAuthors.addAll(authorRepository.save(authors));
        }
        return resolvedAuthors;
    }
}
