package cn.guet.ksmvcmain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ArticlesData {


    private Long view;
    private Long like;
    private Long comment;
}
