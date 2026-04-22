package com.yukibun.yukidoc.Entity;

import lombok.*;

@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class Post {
    int post_id;
    String post_title;
    String post_content;
    int owner_id;
    String owner_name;
    String file_url;
    String file_name;

}
