package edu.irp.portal;

import static edu.irp.portal.RecordStatus.*;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Role-based status workflow: DRAFT -> SUBMITTED -> APPROVED/REJECTED -> PUBLISHED. */
@Service
public class WorkflowService {

    private record Rule(RecordStatus from, RecordStatus to, Set<String> roles) { }

    private static final List<Rule> RULES = List.of(
        new Rule(DRAFT, SUBMITTED, Set.of("SUBMITTER", "ADMIN")),
        new Rule(SUBMITTED, APPROVED, Set.of("REVIEWER", "ADMIN")),
        new Rule(SUBMITTED, REJECTED, Set.of("REVIEWER", "ADMIN")),
        new Rule(REJECTED, DRAFT, Set.of("SUBMITTER", "ADMIN")),
        new Rule(APPROVED, PUBLISHED, Set.of("ADMIN")));

    public String normalize(String role) {
        return role == null ? "VIEWER" : role.trim().toUpperCase();
    }

    public boolean canCreate(String role) {
        return Set.of("SUBMITTER", "ADMIN").contains(role);
    }

    public boolean canEdit(RecordStatus status, String role) {
        return canCreate(role) && (status == DRAFT || status == REJECTED);
    }

    public boolean isValidMove(RecordStatus from, RecordStatus to) {
        return RULES.stream().anyMatch(r -> r.from() == from && r.to() == to);
    }

    public boolean mayMove(RecordStatus from, RecordStatus to, String role) {
        return RULES.stream().anyMatch(r -> r.from() == from && r.to() == to && r.roles().contains(role));
    }

    public List<RecordStatus> allowed(RecordStatus from, String role) {
        return RULES.stream()
            .filter(r -> r.from() == from && r.roles().contains(role))
            .map(Rule::to)
            .toList();
    }
}
