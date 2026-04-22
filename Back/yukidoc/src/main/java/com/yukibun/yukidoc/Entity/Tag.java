package com.yukibun.yukidoc.Entity;

import lombok.*;

@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class Tag {
    Integer tag_id;
    String tag_name;
    String tag_content;
}
