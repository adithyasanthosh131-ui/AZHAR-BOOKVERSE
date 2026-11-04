
package librarymanagement.controller;

import librarymanagement.model.Book;
import librarymanagement.model.BorrowRecord;
import librarymanagement.repository.BookRepository;
import librarymanagement.repository.BorrowRecordRepository;
import librarymanagement.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminController {

    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;
    private final BorrowRecordRepository borrowRepository;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminController(
            BookRepository bookRepository,
            StudentRepository studentRepository,
            BorrowRecordRepository borrowRepository) {

        this.bookRepository = bookRepository;
        this.studentRepository = studentRepository;
        this.borrowRepository = borrowRepository;
    }

    private boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(
                session.getAttribute("adminLoggedIn"));
    }

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (adminUsername.equals(username.trim())
                && adminPassword.equals(password)) {

            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminName", "Administrator");

            return "redirect:/admin/dashboard";
        }

        model.addAttribute("error", "Invalid admin username or password.");
        return "admin/admin-login";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.removeAttribute("adminLoggedIn");
        session.removeAttribute("adminName");
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("adminName",
                session.getAttribute("adminName"));

        model.addAttribute("totalBooks", bookRepository.count());
        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("totalBorrowed",
                borrowRepository.countByStatus("BORROWED"));
        model.addAttribute("totalReturned",
                borrowRepository.countByStatus("RETURNED"));

        model.addAttribute("totalOverdue",
                borrowRepository.findByStatusAndDueDateBefore(
                        "BORROWED", LocalDate.now()).size());

        return "admin/dashboard";
    }

    // BOOK MANAGEMENT

    @GetMapping("/admin/books")
    public String manageBooks(
            HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("books", bookRepository.findAll());
        return "admin/books";
    }

    @GetMapping("/admin/books/new")
    public String newBook(
            HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("book", new Book());
        model.addAttribute("pageTitle", "Add New Book");

        return "admin/book-form";
    }

    @PostMapping("/admin/books/save")
    public String saveBook(
            @ModelAttribute Book book,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        if (book.getTotalCopies() == null
                || book.getTotalCopies() < 0) {
            model.addAttribute("book", book);
            model.addAttribute("pageTitle", "Add or Edit Book");
            model.addAttribute("error", "Enter a valid total copy count.");
            return "admin/book-form";
        }

        if (book.getId() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        } else {
            Book oldBook = bookRepository.findById(book.getId())
                    .orElse(null);

            if (oldBook == null) {
                return "redirect:/admin/books";
            }

            int alreadyBorrowed =
                    oldBook.getTotalCopies()
                    - oldBook.getAvailableCopies();

            if (book.getTotalCopies() < alreadyBorrowed) {
                model.addAttribute("book", book);
                model.addAttribute("pageTitle", "Edit Book");
                model.addAttribute("error",
                        "Total copies cannot be less than borrowed copies.");
                return "admin/book-form";
            }

            book.setAvailableCopies(
                    book.getTotalCopies() - alreadyBorrowed);
        }

        bookRepository.save(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/admin/books/edit/{id}")
    public String editBook(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Book book = bookRepository.findById(id).orElse(null);

        if (book == null) {
            return "redirect:/admin/books";
        }

        model.addAttribute("book", book);
        model.addAttribute("pageTitle", "Edit Book");

        return "admin/book-form";
    }

    @PostMapping("/admin/books/delete/{id}")
    public String deleteBook(
        @PathVariable Long id,
        HttpSession session) {

    if (!isAdmin(session)) {
        return "redirect:/admin/login";
    }

    Book book = bookRepository.findById(id).orElse(null);

    if (book == null) {
        return "redirect:/admin/books";
    }

    if (borrowRepository.countByBook_Id(id) > 0) {
        return "redirect:/admin/books?error=history";
    }

    bookRepository.delete(book);

    return "redirect:/admin/books";
}

    // STUDENT MANAGEMENT

    @GetMapping("/admin/students")
    public String manageStudents(
            HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("students", studentRepository.findAll());
        return "admin/students";
    }
    // EDIT STUDENT

@GetMapping("/admin/students/edit/{id}")
public String editStudent(
        @PathVariable Long id,
        HttpSession session,
        Model model) {

    if (!isAdmin(session)) {
        return "redirect:/admin/login";
    }

    var student = studentRepository.findById(id).orElse(null);

    if (student == null) {
        return "redirect:/admin/students";
    }

    model.addAttribute("student", student);

    return "admin/student-form";
}

// UPDATE STUDENT

@PostMapping("/admin/students/update/{id}")
public String updateStudent(
        @PathVariable Long id,
        @RequestParam String name,
        @RequestParam String email,
        @RequestParam String department,
        @RequestParam Integer semester,
        HttpSession session,
        Model model) {

    if (!isAdmin(session)) {
        return "redirect:/admin/login";
    }

    var student = studentRepository.findById(id).orElse(null);

    if (student == null) {
        return "redirect:/admin/students";
    }

    var existingStudent =
            studentRepository.findByEmailIgnoreCase(email.trim());

    if (existingStudent.isPresent()
            && !existingStudent.get().getId().equals(id)) {

        model.addAttribute("student", student);
        model.addAttribute("error", "This email is already registered.");
        return "admin/student-form";
    }

    student.setName(name.trim());
    student.setEmail(email.trim());
    student.setDepartment(department.trim());
    student.setSemester(semester);

    studentRepository.save(student);

    return "redirect:/admin/students?updated=true";
}

    // BORROWING MANAGEMENT

    @GetMapping("/admin/borrow")
    public String borrowRecords(
            HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("records", borrowRepository.findAll());
        return "admin/borrow-records";
    }

    @PostMapping("/admin/return/{id}")
    @Transactional
    public String adminReturnBook(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        BorrowRecord record = borrowRepository.findById(id)
                .orElse(null);

        if (record == null || !"BORROWED".equals(record.getStatus())) {
            return "redirect:/admin/borrow";
        }

        record.setStatus("RETURNED");
        record.setReturnDate(LocalDate.now());

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        borrowRepository.save(record);
        bookRepository.save(book);

        return "redirect:/admin/borrow";
    }

    // OVERDUE BOOKS

    @GetMapping("/admin/overdue")
    public String overdueBooks(
            HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        List<BorrowRecord> overdueRecords =
                borrowRepository.findByStatusAndDueDateBefore(
                        "BORROWED", LocalDate.now());

        model.addAttribute("records", overdueRecords);
        return "admin/overdue";
    }

    // REPORTS

    @GetMapping("/admin/reports")
    public String reports(
            HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("totalBooks", bookRepository.count());
        model.addAttribute("totalStudents", studentRepository.count());

        model.addAttribute("totalBorrowed",
                borrowRepository.countByStatus("BORROWED"));

        model.addAttribute("totalReturned",
                borrowRepository.countByStatus("RETURNED"));

        model.addAttribute("totalOverdue",
                borrowRepository.findByStatusAndDueDateBefore(
                        "BORROWED", LocalDate.now()).size());

        return "admin/reports";
    }
}
