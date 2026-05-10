package assistant.entity;


import lombok.Data;

@Data
public class SubjectCoverageDTO {
    private Integer subId;
    private String subName;
    private Integer totalKp;
    private Integer practicedKp;
}