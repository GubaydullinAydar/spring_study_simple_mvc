package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BookService {

    private final Logger logger = Logger.getLogger(BookService.class);

    private final ProjectRepository<Book> bookRepo;

    @Autowired
    public BookService(BookRepository<Book> bookRepo) {
        this.bookRepo = bookRepo;
    }

    public List<Book> getAllBooks() {
        return bookRepo.retrieveAll();
    }

    public boolean saveBook(Book book) {
        if (StringUtils.isEmpty(book.getAuthor()) && StringUtils.isEmpty(book.getTitle()) && Objects.isNull(book.getSize())) {
            logger.info("Save error! All fields null!");
            return false;
        }

        bookRepo.store(book);
        return true;
    }

    public boolean removeBookById(Integer bookIdToRemove) {
        return bookRepo.removeItemById(bookIdToRemove);
    }

    public boolean removeBookByRegex(String queryRegex) {
        try {
            Pattern pattern = Pattern.compile(queryRegex);

            int bookRemoved = 0;

            for (Book book : getAllBooks()) {
                if (isMatches(pattern, book.getAuthor()) || isMatches(pattern, book.getTitle())
                        || isMatches(pattern, book.getSize().toString())) {
                    logger.info("remove book completed: " + book);
                    removeBookById(book.getId());
                    bookRemoved++;
                }
            }
            return bookRemoved > 0;
        } catch (Exception e) {
            logger.error("Error remove book by regex");
            throw e;
        }
    }

    private boolean isMatches(Pattern pattern, String field) {
        Matcher matcher = pattern.matcher(field);
        return matcher.matches();
    }

    private void defaultInit() {
        logger.info("default INIT in book service bean");
    }

    private void defaultDestroy() {
        logger.info("default DESTROY in in book service bean");
    }
}
