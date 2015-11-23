package de.axelspringer.publishing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.axelspringer.publishing.json.CustomDateDeserializer;
import de.axelspringer.publishing.json.CustomDateSerializer;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@JsonIgnoreProperties(value = { "handler", "hibernateLazyInitializer" })
@Proxy(lazy=false)
public class Article {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Integer id;
    private String header;
    private String description;
    private String text;
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private OffsetDateTime published;
    @ManyToMany(fetch=FetchType.EAGER)
    private Set<Author> authors;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> keywords;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OffsetDateTime getPublished() {
        return published;
    }

    public void setPublished(OffsetDateTime published) {
        this.published = published;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }
}
