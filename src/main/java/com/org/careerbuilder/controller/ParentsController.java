package com.org.careerbuilder.controller;
import com.org.careerbuilder.models.Parent;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import com.org.careerbuilder.service.ParentService;


@Slf4j
@RestController
@RequestMapping("/api/parents")
public class ParentsController {

//	private static final Logger log = LoggerFactory.getLogger(ParentsController.class);

    private final ParentService service;

    public ParentsController(ParentService service) {
        this.service = service;
    }

    @PostMapping
    public Parent create(@Valid @RequestBody Parent parents) {
        log.info("API: Create parent request id={}", parents.getId());
        return service.create(parents);
    }

    @GetMapping
    public Page<Parent> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        log.info("API: Get parents list");
        return service.getAll(page, size, sortBy);
    }

    @GetMapping("/{id}")
    public Parent getById(@PathVariable Long id) {
        log.info("API: Get Parents by id={}", id);
        return service.getById(id);
    }

    @GetMapping("/by-parent-id/{parentId}")
    public Parent getByParentId(@PathVariable String parentId) {
        log.info("API: Get parent by parentId={}", parentId);
        return service.getByParentId(parentId);
    }

    @PutMapping("/{id}")
    public Parent update(@PathVariable Long id, @Valid @RequestBody Parent parents) {
        log.info("API: Update parent id={}", id);
        return service.update(id, parents);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        log.info("API: Delete parent id={}", id);
        service.delete(id);
        return "Parent deleted successfully";
    }
}
