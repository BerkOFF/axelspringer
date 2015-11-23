package de.axelspringer.publishing.controller;

import com.google.common.collect.ImmutableMap;
import de.axelspringer.publishing.model.Article;
import de.axelspringer.publishing.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @RequestMapping
    Map<Integer, Article> list() {
        return articleService.findAll().stream().collect(Collectors.toMap(Article::getId, a -> a));
    }

    @RequestMapping(value = "/search")
    Map<String, List<Integer>> search(@RequestParam(required = false) List<String> keywords,
                                      @RequestParam(required = false) List<String> authors,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime publishedSince,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime publishedBefore) {
        if(keywords != null && !keywords.isEmpty() ||
                authors!=null && !authors.isEmpty() ||
                publishedSince != null || publishedBefore != null) {
            return ImmutableMap.of("results", articleService.find(keywords, authors, publishedSince, publishedBefore).stream().map(Article::getId).collect(Collectors.toList()));
        }
        throw new IllegalArgumentException("Search criteria is not provided");
    }

    @RequestMapping(method = RequestMethod.POST)
    Map<String, Integer> create(@RequestBody Article article) {
        return ImmutableMap.of("id", articleService.create(article).getId());
    }

    @RequestMapping(value = "/{id}")
    Article display(@PathVariable("id") Integer id) {
        return articleService.read(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    void update(@RequestBody Article article, @PathVariable("id") Integer id) {
        article.setId(id);
        articleService.update(article);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable("id") Integer id) {
        articleService.delete(id);
    }
}
