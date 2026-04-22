package com.yukibun.yukidoc.Entity;

import lombok.*;

import java.util.List;

@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class Doc {
    int doc_id;
    int owner_id;
    Integer connected_note_id;
    int is_folder;
    int father_id;
    String doc_name;
    String doc_url;
    String doc_content;
    double doc_match_score;
    List<Integer> doc_tags;
}
