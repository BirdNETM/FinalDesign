package com.yukibun.yukidoc.Entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    int uid;
    String user_name;
    String user_email;
    String password;
}
