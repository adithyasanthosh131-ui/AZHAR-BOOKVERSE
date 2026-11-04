package librarymanagement.controller;

import librarymanagement.model.Student;
import librarymanagement.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentDashboardController {

    private final StudentRepository studentRepository;

    public StudentDashboardController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Long studentId = (Long) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("student", student);
        model.addAttribute("name", student.getName());

        return "dashboard";
    }
}