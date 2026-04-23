package assistant.controller;

import assistant.entity.Subject;
import assistant.mapper.SubjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class subjectsController {

    @Autowired
    private SubjectMapper subjectMapper;

    @GetMapping("/subjects") // 设置网页访问路径为 /subjects
    public List<Subject> getAllSubjects() {
        System.out.println("Loading subjects...");
        List<Subject> result = subjectMapper.selectList(null);
        System.out.println("Loaded " + result.size() + " subjects");
        return result;
    }
}