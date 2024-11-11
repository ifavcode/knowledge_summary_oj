package cn.guet.ksmvcmain.controller;

import cn.guet.ksmvcmain.entity.vo.ExecResult;
import cn.guet.ksmvcmain.entity.vo.QuestionFile;
import cn.guet.ksmvcmain.entity.vo.Questions;
import cn.guet.ksmvcmain.entity.vo.Submissions;
import cn.guet.ksmvcmain.service.ExecResultService;
import cn.guet.ksmvcmain.service.QuestionFileService;
import cn.guet.ksmvcmain.service.QuestionsService;
import cn.guet.ksmvcmain.service.SubmissionsService;
import cn.guet.ksmvcmain.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.kscommon.entity.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionsService questionsService;
    private final ExecResultService execResultService;
    private final SubmissionsService submissionsService;
    private final QuestionFileService questionFileService;

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false, defaultValue = "1") Integer page,
                           @RequestParam(required = false, defaultValue = "30") Integer limit,
                           @RequestParam(required = false, defaultValue = "") String search) {
        Page<Questions> paged = questionsService.getQuestionsPage(new Page<>(page, limit), search);
        List<Questions> records = paged.getRecords();
        List<String> questionCodeList = records.stream().map(Questions::getQuestionCode).toList();
        Map<String, ExecResult> codeToAcResult = execResultService.getLastAcUploadList(questionCodeList).stream()
                .collect(Collectors.toMap(v -> v.getSubmissions().getQuestionCode(), Function.identity()));
        Map<String, ExecResult> codeToResult = execResultService.getLastUploadList(questionCodeList).stream()
                .collect(Collectors.toMap(v -> v.getSubmissions().getQuestionCode(), Function.identity()));
        List<Submissions> submissions = submissionsService.getSubmissionsByQuestionCodeList(questionCodeList);
        Map<String, List<Submissions>> codeToSubmissionsMap = new HashMap<>();
        for (Submissions submission : submissions) {
            codeToSubmissionsMap.computeIfAbsent(submission.getQuestionCode(), v -> new ArrayList<>()).add(submission);
        }
        for (Questions questions : records) {
            int totalAc = 0;
            int totalTry = 0;
            if (codeToSubmissionsMap.containsKey(questions.getQuestionCode())) {
                totalAc = (int) codeToSubmissionsMap.get(questions.getQuestionCode()).stream()
                        .filter(v -> v.getExecResults() != null && v.getExecResults().getTotalCnt() - v.getExecResults().getAcCnt() == 0).count();
                totalTry = codeToSubmissionsMap.get(questions.getQuestionCode()).size();
            }
            questions.setTotalAc(totalAc);
            questions.setTotalTry(totalTry);
            if (codeToAcResult.containsKey(questions.getQuestionCode())) {
                ExecResult lastAcUpload = codeToAcResult.get(questions.getQuestionCode());
                questions.setAcFlag(true);
                questions.setQuestionDesc("100% AC");
                questions.setToken(lastAcUpload.getSubmissions().getToken());
            } else {
                questions.setAcFlag(false);
                ExecResult lastUpload = codeToResult.get(questions.getQuestionCode());
                if (lastUpload != null) {
                    int total = lastUpload.getTotalCnt();
                    String[] moreCntAndName = lastUpload.getMoreCntAndName();
                    questions.setQuestionDesc(String.format("%s%% %s", Integer.parseInt(moreCntAndName[1]) / total * 100, moreCntAndName[0]));
                    questions.setToken(lastUpload.getSubmissions().getToken());
                }
            }
        }
        return AjaxResult.success(paged);
    }

    @GetMapping("/code")
    public AjaxResult getByCode(@RequestParam String questionCode) {
        Questions questions = questionsService.getOne(Wrappers.<Questions>lambdaQuery().eq(Questions::getQuestionCode, questionCode));
        ExecResult lastAcUpload = execResultService.getLastAcUpload(questionCode);
        if (lastAcUpload != null) {
            questions.setAcFlag(true);
            questions.setQuestionDesc("100% AC");
            questions.setToken(lastAcUpload.getSubmissions().getToken());
        } else {
            questions.setAcFlag(false);
            ExecResult lastUpload = execResultService.getLastUpload(questionCode);
            if (lastUpload != null) {
                int total = lastUpload.getTotalCnt();
                String[] moreCntAndName = lastUpload.getMoreCntAndName();
                questions.setQuestionDesc(String.format("%s%% %s", Integer.parseInt(moreCntAndName[1]) / total * 100, moreCntAndName[0]));
                questions.setToken(lastUpload.getSubmissions().getToken());
            }
        }
        return AjaxResult.success(questions);
    }

    @GetMapping("/lastAcUpload/{questionCode}")
    public AjaxResult getLastAcUpload(@PathVariable String questionCode) {
        return AjaxResult.success(execResultService.getLastAcUpload(questionCode));
    }

    @GetMapping("/lastUpload/{questionCode}")
    public AjaxResult getLastUpload(@PathVariable String questionCode) {
        return AjaxResult.success(execResultService.getLastUpload(questionCode));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('*')")
    public AjaxResult edit(@RequestBody Questions questions) {
        boolean updated = questionsService.update(questions, Wrappers.<Questions>lambdaUpdate().eq(Questions::getQuestionCode, questions.getQuestionCode()));
        if (updated) {
            return AjaxResult.success(true);
        }
        return AjaxResult.error(false);
    }

    @PostMapping("/publish")
    @PreAuthorize("hasAuthority('*')")
    public AjaxResult publish(@RequestBody Questions questions) {
        Questions one = questionsService.getOne(Wrappers.<Questions>lambdaQuery().eq(Questions::getQuestionCode, questions.getQuestionCode()));
        if (one != null) {
            return AjaxResult.error("问题编号已存在");
        }
        questions.setUserId(SecurityUtil.getUserId());
        questions.setCreateTime(new Date());
        boolean save = questionsService.save(questions);
        if (save) {
            return AjaxResult.success(questions);
        }
        return AjaxResult.error();
    }

    @GetMapping("/testcase")
    public AjaxResult testcase(@RequestParam String questionCode) {
        return AjaxResult.success(questionFileService.getQuestionFile(questionCode));
    }

    @PostMapping("/testcase/{questionCode}")
    @Transactional
    public AjaxResult addTestcase(@RequestBody List<QuestionFile> questionFileList, @PathVariable String questionCode) {
        questionFileService.remove(Wrappers.<QuestionFile>lambdaQuery().eq(QuestionFile::getQuestionCode, questionCode));
        if (questionFileList.isEmpty()) {
            return AjaxResult.success(true);
        }
        questionFileService.saveBatch(questionFileList);
        return AjaxResult.success(true);
    }

    @PostMapping("/delete/{questionCodeList}")
    public AjaxResult delete(@PathVariable List<String> questionCodeList) {
        if (questionCodeList == null || questionCodeList.isEmpty()) {
            return AjaxResult.success(true);
        }
        // 逻辑删除，关联内容不删除
        questionsService.remove(Wrappers.<Questions>lambdaQuery().in(Questions::getQuestionCode, questionCodeList));
        return AjaxResult.success(true);
    }

}
