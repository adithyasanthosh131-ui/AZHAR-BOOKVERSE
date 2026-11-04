
package librarymanagement.controller;

import librarymanagement.model.Book;
import librarymanagement.repository.BookRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/books")
    public String books(
            @RequestParam(required = false) String keyword,
            HttpSession session,
            Model model) {

        Long studentId =
                (Long) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        List<Book> books;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String search = keyword.trim();

            books = bookRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                            search, search);
        } else {
            books = bookRepository.findAll();
        }

        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        model.addAttribute(
                "studentName",
                session.getAttribute("studentName"));

        return "books";
    }
}

