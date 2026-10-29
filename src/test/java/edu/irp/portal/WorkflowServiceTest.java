package edu.irp.portal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class WorkflowServiceTest {

    private final WorkflowService wf = new WorkflowService();

    @Test
    void submitterCanSubmitDraft() {
        assertEquals(List.of(RecordStatus.SUBMITTED), wf.allowed(RecordStatus.DRAFT, "SUBMITTER"));
    }

    @Test
    void reviewerCanApproveOrReject() {
        assertTrue(wf.mayMove(RecordStatus.SUBMITTED, RecordStatus.APPROVED, "REVIEWER"));
        assertTrue(wf.mayMove(RecordStatus.SUBMITTED, RecordStatus.REJECTED, "REVIEWER"));
    }

    @Test
    void onlyAdminPublishes() {
        assertFalse(wf.mayMove(RecordStatus.APPROVED, RecordStatus.PUBLISHED, "REVIEWER"));
        assertTrue(wf.mayMove(RecordStatus.APPROVED, RecordStatus.PUBLISHED, "ADMIN"));
    }

    @Test
    void viewerCannotCreateOrEdit() {
        assertFalse(wf.canCreate("VIEWER"));
        assertFalse(wf.canEdit(RecordStatus.DRAFT, "VIEWER"));
    }

    @Test
    void publishedRecordsAreLocked() {
        assertFalse(wf.canEdit(RecordStatus.PUBLISHED, "ADMIN"));
        assertFalse(wf.isValidMove(RecordStatus.PUBLISHED, RecordStatus.DRAFT));
    }
}
