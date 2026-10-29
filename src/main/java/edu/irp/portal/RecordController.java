package edu.irp.portal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class RecordController {

    public record TransitionRequest(@NotNull RecordStatus to) { }

    private final RecordRepository repo;
    private final WorkflowService workflow;
    private final String environment;
    private final String version;
    private final boolean demoDefect;

    public RecordController(RecordRepository repo, WorkflowService workflow,
                            @Value("${app.environment:local}") String environment,
                            @Value("${app.version:dev}") String version,
                            @Value("${app.demo-defect:false}") boolean demoDefect) {
        this.repo = repo;
        this.workflow = workflow;
        this.environment = environment;
        this.version = version;
        this.demoDefect = demoDefect;
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("environment", environment, "version", version);
    }

    @GetMapping("/records")
    public List<RecordView> list(@RequestParam(defaultValue = "") String q,
                                 @RequestParam(required = false) RecordStatus status,
                                 @RequestHeader(value = "X-Role", defaultValue = "VIEWER") String roleHeader) {
        String role = workflow.normalize(roleHeader);
        // Deliberate defect for Task 10 (Continuous Testing). Enabled only with APP_DEMO_DEFECT=true.
        if (demoDefect && !q.isBlank()) {
            return List.of();
        }
        List<RepositoryRecord> found = status == null ? repo.search(q.trim()) : repo.searchByStatus(q.trim(), status);
        return found.stream().map(r -> view(r, role)).toList();
    }

    @GetMapping("/records/{id}")
    public RecordView get(@PathVariable Long id,
                          @RequestHeader(value = "X-Role", defaultValue = "VIEWER") String roleHeader) {
        return view(find(id), workflow.normalize(roleHeader));
    }

    @PostMapping("/records")
    @ResponseStatus(HttpStatus.CREATED)
    public RecordView create(@Valid @RequestBody RecordRequest req,
                             @RequestHeader(value = "X-Role", defaultValue = "VIEWER") String roleHeader) {
        String role = workflow.normalize(roleHeader);
        if (!workflow.canCreate(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role " + role + " cannot create records.");
        }
        RepositoryRecord record = new RepositoryRecord();
        apply(record, req);
        record.setStatus(RecordStatus.DRAFT);
        return view(repo.save(record), role);
    }

    @PutMapping("/records/{id}")
    public RecordView update(@PathVariable Long id, @Valid @RequestBody RecordRequest req,
                             @RequestHeader(value = "X-Role", defaultValue = "VIEWER") String roleHeader) {
        String role = workflow.normalize(roleHeader);
        RepositoryRecord record = find(id);
        if (!workflow.canEdit(record.getStatus(), role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Role " + role + " cannot edit a record in status " + record.getStatus() + ".");
        }
        apply(record, req);
        record.setVersionNo(record.getVersionNo() + 1);
        return view(repo.save(record), role);
    }

    @PostMapping("/records/{id}/transition")
    public RecordView transition(@PathVariable Long id, @Valid @RequestBody TransitionRequest req,
                                 @RequestHeader(value = "X-Role", defaultValue = "VIEWER") String roleHeader) {
        String role = workflow.normalize(roleHeader);
        RepositoryRecord record = find(id);
        if (!workflow.isValidMove(record.getStatus(), req.to())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "A record cannot move from " + record.getStatus() + " to " + req.to() + ".");
        }
        if (!workflow.mayMove(record.getStatus(), req.to(), role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Role " + role + " cannot move a record to " + req.to() + ".");
        }
        record.setStatus(req.to());
        return view(repo.save(record), role);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (RecordStatus s : RecordStatus.values()) {
            byStatus.put(s.name(), 0L);
        }
        for (Object[] row : repo.countByStatus()) {
            byStatus.put(((RecordStatus) row[0]).name(), (Long) row[1]);
        }
        Map<String, Long> byDepartment = new LinkedHashMap<>();
        for (Object[] row : repo.countByDepartment()) {
            byDepartment.put(row[0] == null ? "Unassigned" : (String) row[0], (Long) row[1]);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", repo.count());
        result.put("byStatus", byStatus);
        result.put("byDepartment", byDepartment);
        return result;
    }

    private RepositoryRecord find(Long id) {
        return repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record " + id + " was not found."));
    }

    private void apply(RepositoryRecord r, RecordRequest req) {
        r.setTitle(req.title().trim());
        r.setAuthors(req.authors().trim());
        r.setAbstractText(req.abstractText());
        r.setRecordType(req.recordType());
        r.setDepartment(req.department());
        r.setPublicationYear(req.publicationYear());
        r.setKeywords(req.keywords());
    }

    private RecordView view(RepositoryRecord r, String role) {
        return new RecordView(r.getId(), r.getTitle(), r.getAuthors(), r.getAbstractText(), r.getRecordType(),
            r.getDepartment(), r.getPublicationYear(), r.getKeywords(), r.getStatus(), r.getHandleId(),
            r.getVersionNo(), r.getUpdatedAt(), workflow.canEdit(r.getStatus(), role),
            workflow.allowed(r.getStatus(), role));
    }
}
