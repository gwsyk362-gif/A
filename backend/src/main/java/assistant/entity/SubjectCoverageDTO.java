package assistant.entity;


import lombok.Data;

@Data
public class SubjectCoverageDTO {
    private Integer subId;
    private String subName;
    private Integer totalKp;      // 该科目总知识点数
    private Integer practicedKp;  // 用户已练习的知识点数
}