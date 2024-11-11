package cn.guet.ksmvcmain.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSubmitCode {

    @NotNull
    private String questionCode;
    @NotNull
    private String sourceCode;
    @NotNull
    private Integer languageId;

    private String stdin;
}
