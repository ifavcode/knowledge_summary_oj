package cn.guet.ksmvcmain.entity;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmitCode {

    @NotNull
    private String sourceCode;
    @NotNull
    private Integer languageId;
    private String compilerOptions = null;
    private String commandLineArguments = null;
    private String stdin;
    private String expectedOutput;
    private Float cpuTimeLimit;
    private Float cpuExtraTime;
    private float wallTimeLimit;
    private float memoryLimit;
    private int stackLimit;
    private int maxProcessesAndOrThreads;
    private boolean enablePerProcessAndThreadTimeLimit;
    private boolean enablePerProcessAndThreadMemoryLimit;
    private int maxFileSize;
    private boolean redirectStdErrToStdOut;
    private boolean enableNetwork;
    private int numberOfRuns;
    private String additionalFiles;
    private String callbackUrl;
    private String stdOut;
    private String stdErr;
    private String compileOutput;
    private String message;
    private int exitCode;
    private int exitSignal;
    private Object status;
    private Date createdAt;
    private Date finishedAt;
    private String token;
    private float time;
    private float wallTime;
    private float memory;

}
