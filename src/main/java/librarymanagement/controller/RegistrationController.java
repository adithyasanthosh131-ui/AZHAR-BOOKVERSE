package librarymanagement.controller;

import librarymanagement.model.Student;
import librarymanagement.repository.StudentRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/register";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerStudent(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String department,
            @RequestParam Integer semester,
            @RequestParam String password,
            Model model) {

        if (studentRepository.findByEmailIgnoreCase(
                email.trim()).isPresent()) {

            model.addAttribute(
                    "error",
                    "Email already registered. Please login.");

            return "register";
        }

        Student student = new Student();

        student.setName(name.trim());
        student.setEmail(email.trim());
        student.setDepartment(department.trim());
        student.setSemester(semester); 
        student.setPassword(
                passwordEncoder.encode(password));

        studentRepository.save(student);

        return "redirect:/login?registered";
    }
}