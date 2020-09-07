package com.mpip.bookexplorer.Models;

import java.util.ArrayList;
import java.util.List;

public class Book {
    String title;
    List<String> authors;
    String description;
    int pageCount;
    String poster;
    String ISBN;
    String datePublished;
    String publisher;

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Book() {
        this.authors=new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    public Book(String title, List<String> authors, String description, int pageCount, String poster, String ISBN, String datePublished,String publisher) {
        this.title = title;
        this.authors = authors;
        this.description = description;
        this.pageCount = pageCount;
        this.poster = poster;
        this.ISBN = ISBN;
        this.datePublished = datePublished;
        this.publisher=publisher;
    }
}
