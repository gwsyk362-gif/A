package assistant.controller;

import assistant.entity.Question;
import assistant.entity.Subject;
import assistant.mapper.QuestionMapper;
import assistant.mapper.SubjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class QuestionController {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    // 获取所有题目（保留，若有其他用途）
    @GetMapping("/allQuestions")
    public List<Question> getAllQuestions() {
        return questionMapper.selectList(null);
    }

    // 新增题目
    @PostMapping("/addQuestion")
    public String addQuestion(@RequestBody Question question) {
        questionMapper.insert(question);
        return "新增成功";
    }

    // 更新题目
    @PostMapping("/updateQuestion")
    public String updateQuestion(@RequestBody Question question) {
        questionMapper.updateById(question);
        return "更新成功";
    }

    // 删除题目
    @DeleteMapping("/deleteQuestion")
    public String deleteQuestion(@RequestParam("id") Integer id) {
        questionMapper.deleteById(id);
        return "删除成功";
    }

    // 获取所有科目
    @GetMapping("/allSubjects")
    public List<Subject> getAllSubjects() {
        return subjectMapper.selectList(null);
    }

    // 查询知识点
    @GetMapping("/getKpBySubject")
    public List<String> getKpBySubject(@RequestParam("subId") Integer subId) {
        return questionMapper.selectDistinctKpBySubject(subId);
    }

    // 分页查询题目（支持科目、知识点、题干关键词搜索）
    @GetMapping("/questions/page")
    public Map<String, Object> getQuestionsPage(
            @RequestParam("current") Integer current,
            @RequestParam("size") Integer size,
            @RequestParam(value = "subId", required = false) Integer subId,
            @RequestParam(value = "kp", required = false) String kp,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Page<Question> page = new Page<>(current, size);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();

        if (subId != null) {
            queryWrapper.eq("ques_sub_id", subId);
        }
        if (kp != null && !kp.isEmpty()) {
            queryWrapper.like("ques_kp", kp);
        }
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("ques_content", keyword);
        }

        queryWrapper.orderByAsc("ques_id"); // 按ID降序排列
        Page<Question> resultPage = questionMapper.selectPage(page, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        return result;
    }
}