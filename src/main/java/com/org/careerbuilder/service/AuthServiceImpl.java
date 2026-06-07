package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.LoginRequest;
import com.org.careerbuilder.dto.request.RegisterRequest;
import com.org.careerbuilder.dto.response.AuthResponse;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.AppUser;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.School;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.enums.UserRole;
import com.org.careerbuilder.repository.AppUserRepository;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.repository.SchoolRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String INDEPENDENT_SCHOOL_CODE = "INDEPENDENT_LEARNERS";

    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String mobile = normalizeMobile(request.mobile());

        if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
            throw new IllegalArgumentException("Either email or mobile is required.");
        }

        if (email != null && appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        if (mobile != null && appUserRepository.existsByMobile(mobile)) {
            throw new IllegalArgumentException("Mobile is already registered.");
        }

        UserRole role = parseRole(request.role());

        Student linkedStudent = resolveLinkedStudent(request, role, email, mobile);
        School adminSchool = resolveSchoolForAdmin(request, role);
        resolveFacultyForTeacherRegistration(request, role, email, mobile);

        AppUser user = AppUser.builder()
                .email(email)
                .mobile(mobile)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .student(linkedStudent)
                .school(adminSchool)
                .active(true)
                .build();

        AppUser saved;
        try {
            saved = appUserRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            if (linkedStudent != null) {
                throw new IllegalArgumentException("This student profile is already linked to another login account.");
            }
            throw ex;
        }
        String token = jwtService.generateToken(saved);

        return new AuthResponse(
                token,
                "Bearer",
                saved.getId(),
                saved.getStudent() != null ? saved.getStudent().getId() : null,
                resolveFacultyId(saved),
                resolveSchoolId(saved),
                saved.getRole().name(),
                saved.getEmail(),
                saved.getMobile(),
                "Registration successful"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String username = request.username() == null ? "" : request.username().trim();
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        AppUser user = isEmailLike(username)
                ? appUserRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."))
                : appUserRepository.findByMobile(username)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is inactive.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getStudent() != null ? user.getStudent().getId() : null,
                resolveFacultyId(user),
                resolveSchoolId(user),
                user.getRole().name(),
                user.getEmail(),
                user.getMobile(),
                "Login successful"
        );
    }

    private UserRole parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }

        String normalized = rawRole.trim().toUpperCase().replace(' ', '_');

        return switch (normalized) {
            case "STUDENT" -> UserRole.STUDENT;
            case "PARENT" -> UserRole.PARENT;
            case "SCHOOL_ADMIN" -> UserRole.SCHOOL_ADMIN;
            case "TEACHER" -> UserRole.TEACHER;
            case "ADMIN" -> UserRole.ADMIN;
            default -> throw new IllegalArgumentException(
                    "Invalid role. Allowed: STUDENT, PARENT, TEACHER, SCHOOL_ADMIN, ADMIN");
        };
    }

    /**
     * Maps TEACHER login to faculty PK via matching email or mobile on the faculty record.
     */
    private Long resolveFacultyId(AppUser user) {
        if (user.getRole() != UserRole.TEACHER) {
            return null;
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            Optional<Faculty> byEmail = facultyRepository.findFirstByEmailIgnoreCase(user.getEmail().trim());
            if (byEmail.isPresent()) {
                return byEmail.get().getId();
            }
        }
        if (user.getMobile() != null && !user.getMobile().isBlank()) {
            return facultyRepository.findFirstByPhone(user.getMobile().trim())
                    .map(Faculty::getId)
                    .orElse(null);
        }
        return null;
    }

    private School resolveSchoolForAdmin(RegisterRequest request, UserRole role) {
        if (role != UserRole.SCHOOL_ADMIN && role != UserRole.ADMIN) {
            return null;
        }
        if (request.schoolId() == null) {
            throw new IllegalArgumentException("schoolId is required for admin registration.");
        }
        return schoolRepository.findById(request.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + request.schoolId()));
    }

    /**
     * Teacher self-registration must match an existing faculty row for the selected school
     * (created by school admin). Login is linked via matching email or mobile on faculty.
     */
    private Faculty resolveFacultyForTeacherRegistration(
            RegisterRequest request, UserRole role, String email, String mobile) {
        if (role != UserRole.TEACHER) {
            return null;
        }
        if (request.schoolId() == null) {
            throw new IllegalArgumentException("schoolId is required for teacher registration.");
        }
        Long schoolId = request.schoolId();
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found with id: " + schoolId);
        }

        Faculty faculty = null;
        if (email != null) {
            faculty = facultyRepository.findFirstByEmailIgnoreCaseAndSchool_Id(email, schoolId).orElse(null);
        }
        if (faculty == null && mobile != null) {
            faculty = facultyRepository.findFirstByPhoneAndSchool_Id(mobile, schoolId).orElse(null);
        }
        if (faculty == null) {
            throw new IllegalArgumentException(
                    "No faculty profile found for this school with your email or mobile. "
                            + "Ask your school admin to add you as a teacher first, then register using the same contact details.");
        }
        return faculty;
    }

    private Long resolveSchoolId(AppUser user) {
        if (user.getSchool() != null) {
            return user.getSchool().getId();
        }
        if (user.getStudent() != null && user.getStudent().getSchool() != null) {
            return user.getStudent().getSchool().getId();
        }
        Long facultyId = resolveFacultyId(user);
        if (facultyId != null) {
            return facultyRepository.findById(facultyId)
                    .map(f -> f.getSchool() != null ? f.getSchool().getId() : null)
                    .orElse(null);
        }
        return null;
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String normalized = email.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeMobile(String mobile) {
        if (mobile == null) return null;
        String normalized = mobile.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private boolean isEmailLike(String value) {
        return value.contains("@");
    }

    private Student resolveLinkedStudent(RegisterRequest request, UserRole role, String authEmail, String authMobile) {
        if (role != UserRole.STUDENT) {
            return null;
        }

        // Option A: Link to an existing student profile by ID.
        if (request.studentId() != null) {
            Student existingStudent = studentRepository.findById(request.studentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.studentId()));

            if (appUserRepository.existsByStudent_Id(existingStudent.getId())) {
                throw new IllegalArgumentException("This student profile is already linked to another login account.");
            }
            return existingStudent;
        }

        // Option B: First-time registration creates student profile then links FK.
        if (!request.hasStudentProfilePayload()) {
            throw new IllegalArgumentException("For STUDENT role, provide either studentId or complete student profile details.");
        }
        return createStudentFromRequest(request, authEmail, authMobile);
    }

    private Student createStudentFromRequest(RegisterRequest request, String authEmail, String authMobile) {
        String studentEmail = firstNonBlank(request.studentEmail(), authEmail);
        String studentPhone = firstNonBlank(request.studentPhone(), authMobile);

        Student existingStudent = findExistingStudentByContact(studentEmail, studentPhone);
        if (existingStudent != null) {
            if (appUserRepository.existsByStudent_Id(existingStudent.getId())) {
                throw new IllegalArgumentException("This student profile is already linked to another login account.");
            }
            return existingStudent;
        }

        School school = resolveSchoolForStudentRegistration(request.schoolId(), studentEmail, studentPhone);

        Student student = Student.builder()
                .firstName(requireText(request.firstName(), "firstName is required for STUDENT registration"))
                .lastName(requireText(request.lastName(), "lastName is required for STUDENT registration"))
                .age(requireInt(request.age(), "age is required for STUDENT registration"))
                .className(requireText(request.className(), "className is required for STUDENT registration"))
                .section(requireText(request.section(), "section is required for STUDENT registration"))
                .rollNo(requireInt(request.rollNo(), "rollNo is required for STUDENT registration"))
                .parentName(requireText(request.parentName(), "parentName is required for STUDENT registration"))
                .phone(requireText(studentPhone, "studentPhone/mobile is required for STUDENT registration"))
                .email(requireText(studentEmail, "studentEmail/email is required for STUDENT registration"))
                .address(requireText(request.address(), "address is required for STUDENT registration"))
                .school(school)
                .build();

        try {
            return studentRepository.save(student);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Student profile already exists. Please use Student ID or login with existing account.");
        }
    }

    private Student findExistingStudentByContact(String studentEmail, String studentPhone) {
        if (studentEmail != null && !studentEmail.isBlank()) {
            Student byEmail = studentRepository.findFirstByEmailIgnoreCase(studentEmail).orElse(null);
            if (byEmail != null) {
                return byEmail;
            }
        }

        if (studentPhone != null && !studentPhone.isBlank()) {
            return studentRepository.findFirstByPhone(studentPhone).orElse(null);
        }

        return null;
    }

    private School resolveSchoolForStudentRegistration(Long requestedSchoolId, String studentEmail, String studentPhone) {
        if (requestedSchoolId != null) {
            return schoolRepository.findById(requestedSchoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + requestedSchoolId));
        }

        Student existingByContact = null;

        if (studentEmail != null && !studentEmail.isBlank()) {
            existingByContact = studentRepository.findFirstByEmailIgnoreCase(studentEmail).orElse(null);
        }

        if (existingByContact == null && studentPhone != null && !studentPhone.isBlank()) {
            existingByContact = studentRepository.findFirstByPhone(studentPhone).orElse(null);
        }

        if (existingByContact != null && existingByContact.getSchool() != null) {
            return existingByContact.getSchool();
        }

        return getOrCreateIndependentSchool();
    }

    private School getOrCreateIndependentSchool() {
        return schoolRepository.findBySchoolCodeIgnoreCase(INDEPENDENT_SCHOOL_CODE)
                .orElseGet(() -> schoolRepository.save(School.builder()
                        .schoolName("Independent Learners")
                        .schoolCode(INDEPENDENT_SCHOOL_CODE)
                        .schoolType("INDIVIDUAL")
                        .boardAffiliation("SELF_LEARNING")
                        .affiliationNumber("IND-0001")
                        .yearOfEstablishment("2026")
                        .mediumOfInstruction("ENGLISH")
                        .schoolCategory("INDEPENDENT")
                        .villageTownCity("Online")
                        .district("Online")
                        .stateUT("Online")
                        .build()));
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private Integer requireInt(Integer value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String firstNonBlank(String first, String fallback) {
        if (first != null && !first.trim().isBlank()) {
            return first.trim();
        }
        if (fallback != null && !fallback.trim().isBlank()) {
            return fallback.trim();
        }
        return null;
    }
}
