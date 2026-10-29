package edu.irp.portal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecordRequest(
    @NotBlank @Size(max = 220) String title,
    @NotBlank @Size(max = 220) String authors,
    @Size(max = 4000) String abstractText,
    @Size(max = 40) String recordType,
    @Size(max = 120) String department,
    @Min(1900) @Max(2100) Integer publicationYear,
    @Size(max = 1000) String keywords) { }
