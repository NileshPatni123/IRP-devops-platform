package edu.irp.portal;

import java.time.LocalDateTime;
import java.util.List;

public record RecordView(
    Long id, String title, String authors, String abstractText, String recordType,
    String department, Integer publicationYear, String keywords, RecordStatus status,
    String handleId, Integer versionNo, LocalDateTime updatedAt,
    boolean editable, List<RecordStatus> actions) { }
