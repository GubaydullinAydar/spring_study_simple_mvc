package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.services.BookService;
import org.example.web.dto.Book;
import org.example.web.dto.BookIdToRemove;
import org.example.web.dto.BookRegexToRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@Controller
@RequestMapping("/books")
@Scope("singleton")
public class BookShelfController {

    private final Logger logger = Logger.getLogger(BookShelfController.class);
    private final BookService bookService;

    @Autowired
    public BookShelfController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/shelf")
    public String books(Model model) {
        logger.info("got book shelf");
        return getBookShelf(new Book(), new BookIdToRemove(), new BookRegexToRemove(), model);
    }

    @PostMapping("/save")
    public String saveBook(@Valid Book book, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return getBookShelf(book, new BookIdToRemove(), new BookRegexToRemove(), model);
        } else {
            bookService.saveBook(book);
            logger.info("current repository size: " + bookService.getAllBooks().size());
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/remove")
    public String removeBook(@Valid BookIdToRemove bookIdToRemove, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return getBookShelf(new Book(), bookIdToRemove, new BookRegexToRemove(), model);
        } else {
            bookService.removeBookById(bookIdToRemove.getId());
            return "redirect:/books/shelf";
        }

    }

    @PostMapping("/removeByRegex")
    public String removeBookByRegex(@Valid BookRegexToRemove bookRegexToRemove, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return getBookShelf(new Book(), new BookIdToRemove(), bookRegexToRemove, model);
        }

        try {
            bookService.removeBookByRegex(bookRegexToRemove.getQueryRegex());
        } catch (Exception e) {
            logger.error("Books with regex: " + bookRegexToRemove.getQueryRegex() + " not deleted");
            bindingResult.addError(new FieldError("bookRegexToRemove", "queryRegex", "Regex not valid"));
            return getBookShelf(new Book(), new BookIdToRemove(), bookRegexToRemove, model);
        }

        return "redirect:/books/shelf";
    }

    private String getBookShelf(Book book, BookIdToRemove bookIdToRemove, BookRegexToRemove bookRegexToRemove, Model model) {
        model.addAttribute("book", book);
        model.addAttribute("bookIdToRemove", bookIdToRemove);
        model.addAttribute("bookRegexToRemove", bookRegexToRemove);
        model.addAttribute("bookList", bookService.getAllBooks());
        return "book_shelf";
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) throws Exception {
        String name = file.getOriginalFilename();
        byte[] bytes = file.getBytes();

        if (StringUtils.isEmpty(name) || file.isEmpty()) {
            model.addAttribute("error", "File  or fileName is empty");
            return getBookShelf(new Book(), new BookIdToRemove(), new BookRegexToRemove(), model);
        }

        //create dir
        String rootPath = System.getProperty("catalina.home");
        File dir = new File(rootPath + File.separator + "external_uploads");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //create file
        File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
        stream.write(bytes);
        stream.close();

        logger.info("new file saved at: " + serverFile.getAbsolutePath());

        return "redirect:/books/shelf";
    }
}
