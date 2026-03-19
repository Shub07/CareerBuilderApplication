package com.org.careerbuilder.controller;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import com.org.careerbuilder.models.Parents;
import com.org.careerbuilder.service.ParentService;

@Slf4j
@RestController
@RequestMapping("/api/Parents")
public class ParentsController {

//	private static final Logger log = LoggerFactory.getLogger(ParentsController.class);

    private final ParentService service;

    public ParentsController(ParentService service) {
        this.service = service;
    }

    @PostMapping
    public Parents create(@Valid @RequestBody Parents Parents) {
        log.info("API: Create Parents request ParentsId={}",Parents.getId());
        return service.create(Parents);
    }

    @GetMapping
    public Page<Parents> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        log.info("API: Get Parentss list");
        return service.getAll(page, size, sortBy);
    }

    @GetMapping("/{id}")
    public Parents getById(@PathVariable Long id) {
        log.info("API: Get Parents by id={}", id);
        return service.getById(id);
    }

    @GetMapping("/by-ParentsId/{parentsId}")
    public Parents getByParentsId(@PathVariable String parentsId) {
        log.info("API: Get Parents by ParentsId={}", parentsId);
        return service.getByParentId(parentsId);
    }

    @PutMapping("/{id}")
    public Parents update(@PathVariable Long id, @Valid @RequestBody Parents Parents) {
        log.info("API: Update Parents id={}", id);
        return service.update(id, Parents);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        log.info("API: Delete Parents id={}", id);
        service.delete(id);
        return "Parents deleted successfully";
    }
}
