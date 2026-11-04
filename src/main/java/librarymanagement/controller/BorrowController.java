
package librarymanagement.controller;

import librarymanagement.model.Book;
import librarymanagement.model.BorrowRecord;
import librarymanagement.model.Student;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.BorrowRecordRepository;
import librarymanagement.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BorrowController {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRepository;
    private final StudentRepository studentRepository;

    public BorrowController(
            BookRepository bookRepository,
            BorrowRecordRepository borrowRepository,
            StudentRepository studentRepository) {

        this.bookRepository = bookRepository;
        this.borrowRepository = borrowRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/borrow/{id}")
    @Transactional
    public String borrowBook(
            @PathVariable Long id,
            HttpSession session) {

        Long studentId =
                (Long) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        Student student = studentRepository
                .findById(studentId).orElse(null);

        Book book = bookRepository
                .findById(id).orElse(null);

        if (student == null || book == null) {
            return "redirect:/books";
        }

        if (book.getAvailableCopies() == null ||
                book.getAvailableCopies() <= 0) {
            return "redirect:/books?error=unavailable";
        }

        book.setAvailableCopies(
                book.getAvailableCopies() - 1);

        BorrowRecord record = new BorrowRecord();

        record.setStudent(student);
        record.setBook(book);
        record.setIssueDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(14));
        record.setStatus("BORROWED");

        bookRepository.save(book);
        borrowRepository.save(record);

        return "redirect:/my-books";
    }

    @GetMapping("/my-books")
    public String myBooks(
            HttpSession session,
            Model model) {

        Long studentId =
                (Long) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        List<BorrowRecord> records =
                borrowRepository
                        .findByStudent_IdOrderByIssueDateDesc(
                                studentId);

        model.addAttribute("records", records);

        model.addAttribute(
                "studentName",
                session.getAttribute("studentName"));

        return "my-books";
    }

    @PostMapping("/return/{id}")
    @Transactional
    public String returnBook(
            @PathVariable Long id,
            HttpSession session) {

        Long studentId =
                (Long) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        BorrowRecord record = borrowRepository
                .findByIdAndStudent_IdAndStatus(
                        id, studentId, "BORROWED")
                .orElse(null);

        if (record == null) {
            return "redirect:/my-books";
        }

        record.setStatus("RETURNED");
        record.setReturnDate(LocalDate.now());

        Book book = record.getBook();

        book.setAvailableCopies(
                book.getAvailableCopies() + 1);

        borrowRepository.save(record);
        bookRepository.save(book);

        return "redirect:/my-books";
    }
}
