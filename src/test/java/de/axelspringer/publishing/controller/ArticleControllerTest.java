package de.axelspringer.publishing.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.axelspringer.publishing.model.Article;
import de.axelspringer.publishing.model.Author;
import de.axelspringer.publishing.persistence.ArticleRepository;
import de.axelspringer.publishing.persistence.AuthorRepository;
import de.axelspringer.publishing.util.IntegrationTest;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ArticleControllerTest extends IntegrationTest {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    AuthorRepository authorRepository;

    List<Article> articles;
    List<Author> authors;

    @Before
    public void setUp() {
        articleRepository.deleteAll();
        authorRepository.deleteAll();
        authors = authorRepository.save(ImmutableList.of(newAuthor("Author1"), newAuthor("Author2")));
        authorRepository.flush();

        articles = articleRepository.save(Arrays.asList(
                newArticle("Header 1", ImmutableSet.of(authors.get(0), authors.get(1)),
                        ImmutableSet.of("keyword1", "keyword2"), "Description 1", "Text 1", OffsetDateTime.now(ZoneOffset.UTC)),
                newArticle("Header 2", ImmutableSet.of(authors.get(0)),
                    ImmutableSet.of("keyword2", "keyword3"), "Description 2", "Text 2", null),
                newArticle("Header 3", ImmutableSet.of(authors.get(1)),ImmutableSet.of("keyword1", "keyword3"), "Description 3", "Text 3", OffsetDateTime.now(ZoneOffset.UTC).minusYears(1))
                ));
        articleRepository.flush();
    }

    @Test
    public void display() {
        Article article = articles.get(0);
        when().
                get("/articles/{id}", article.getId()).
                then().
                statusCode(HttpStatus.OK.value()).
                body("id", is(nullValue())).
                body("header", Matchers.equalTo(article.getHeader())).
                body("description", Matchers.equalTo(article.getDescription())).
                body("text", Matchers.equalTo(article.getText())).
                body("published", Matchers.equalTo(article.getPublished().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))).
                body("keywords", Matchers.hasSize(2)).
                body("keywords", Matchers.hasItems("keyword1", "keyword2")).
                body("authors", Matchers.hasSize(2)).
                body("authors", Matchers.hasItems("Author1", "Author2"));

        when().
                get("/articles/{id}", -1).
                then().
                statusCode(HttpStatus.NOT_FOUND.value());

        when().
                get("/articles/{id}", "id").
                then().
                statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void list() throws Exception {
        Map response = when().
                get("/articles").
                then().
                statusCode(HttpStatus.OK.value()).extract().as(Map.class);
        assertThat(response.size(), equalTo(3));
    }

    @Test
    public void create() throws Exception {
        Integer id = given().
                contentType("application/json").
                body(newArticle("some header",
                        ImmutableSet.of(authors.get(0)),
                        ImmutableSet.of("keyword2", "keyword3"),
                        "some descr", "some text", null)).
                when().post("/articles").then().
                statusCode(HttpStatus.OK.value()).extract().jsonPath().getInt("id");
        assertThat(id, is(notNullValue()));
        Article article = articleRepository.getOne(id);
        assertThat(article.getDescription(), Matchers.equalTo("some descr"));
        assertThat(article.getHeader(), Matchers.equalTo("some header"));

        given().
                contentType("application/json").
                body("{incorrectPayload: \"}").
                when().post("/articles").then().
                statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void delete() {
        when().delete("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.NO_CONTENT.value());
        when().delete("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void update() {
        given().
                contentType("application/json").
                body(ImmutableMap.of("header", "new header")).
        when().
                put("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.OK.value());
        Article article = articleRepository.getOne(articles.get(0).getId());
        assertThat(article.getHeader(), equalTo("new header"));

        given().
                contentType("application/json").
                body(ImmutableMap.of("header", "new header")).
                when().
                put("/articles/{id}", -1).then().statusCode(HttpStatus.NOT_FOUND.value());

        given().
                contentType("application/json").
                body("{incorrectPayload: \"}").
                when().
                put("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void updateKeywords() {
        Set<String> keywords = ImmutableSet.of("some keyword", "another keyword");
        given().
                contentType("application/json").
                body(ImmutableMap.of("keywords", keywords)).
                when().
                put("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.OK.value());
        Article article = articleRepository.getOne(articles.get(0).getId());
        assertThat(article.getKeywords(), equalTo(keywords));

        given().
                contentType("application/json").
                body("{\"keywords\": null}").
                when().
                put("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.OK.value());
        article = articleRepository.getOne(articles.get(0).getId());
        assertThat(article.getKeywords(), is(empty()));

        given().
                contentType("application/json").
                body("{\"keywords\": []}").
                when().
                put("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.OK.value());
        article = articleRepository.getOne(articles.get(0).getId());
        assertThat(article.getKeywords(), is(empty()));
    }

    @Test
    public void updateAuthors() {
        List<String> authors = ImmutableList.of("some author", "another author");
        given().
                contentType("application/json").
                body(ImmutableMap.of("authors", authors)).
                when().
                put("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.OK.value());
        Article article = articleRepository.getOne(articles.get(0).getId());
        assertThat(article.getAuthors(), hasSize(2));
        assertThat(article.getAuthors().stream().map(Author::getName).collect(Collectors.toList()), hasItems(authors.toArray()));

        given().
                contentType("application/json").
                body("{\"authors\": null}").
                when().
                put("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.OK.value());
        article = articleRepository.getOne(articles.get(0).getId());
        assertThat(article.getAuthors(), is(empty()));

        given().
                contentType("application/json").
                body("{\"authors\": []}").
                when().
                put("/articles/{id}", articles.get(0).getId()).then().statusCode(HttpStatus.OK.value());
        article = articleRepository.getOne(articles.get(0).getId());
        assertThat(article.getAuthors(), is(empty()));
    }

    @Test
    public void find() {
        when().
                get("/articles/search?keywords=keyword1,keyword2,abcdef").
                then().
                statusCode(HttpStatus.OK.value()).
                body("results", hasSize(3)).
                body("results", hasItems(articles.get(0).getId(), articles.get(1).getId(), articles.get(2).getId()));

        when().
                get("/articles/search?authors=Author1,Author2,abcdef").
                then().
                statusCode(HttpStatus.OK.value()).
                body("results", hasSize(3)).
                body("results", hasItems(articles.get(0).getId(), articles.get(1).getId(), articles.get(2).getId()));

        when().
                get("/articles/search?publishedSince="+OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).
                then().
                statusCode(HttpStatus.OK.value()).
                body("results", hasSize(1)).
                body("results", hasItems(articles.get(0).getId()));

        when().
                get("/articles/search?publishedBefore="+OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).
                then().
                statusCode(HttpStatus.OK.value()).
                body("results", hasSize(2)).
                body("results", hasItems(articles.get(0).getId(), articles.get(2).getId()));
    }

    @Test
    public void findComplexCriteria() {
        when().
                get("/articles/search?keywords=keyword1&authors=Author1").
                then().
                statusCode(HttpStatus.OK.value()).
                body("results", hasSize(1)).
                body("results", hasItems(articles.get(0).getId()));
    }

    @Test
    public void findIncorrectDate() {
        when().
                get("/articles/search?publishedBefore=tt").
                then().
                statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void findNoCriteria() {
        when().
                get("/articles/search").
                then().
                statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private Author newAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        return author;
    }

    private Article newArticle(String header, Set<Author> authors, Set<String> keywords, String descr, String text, OffsetDateTime published) {
        Article article = new Article();
        article.setHeader(header);
        article.setDescription(descr);
        article.setText(text);
        article.setAuthors(authors);
        article.setKeywords(keywords);
        article.setPublished(published);
        return article;
    }
}