package com.example.gradox2.presentation.dto.fileProposal;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FileProposalResponse {
    private Long id;
    private String title;
    private String description;
    private String proposer;
    private String subjectName;
    private String course;
    private String type;
    private String status;
}
