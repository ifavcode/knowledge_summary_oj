package cn.guet.ksmvcmain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FileIdAndContent {

    private Integer fileId;
    private String fileContent;
}
