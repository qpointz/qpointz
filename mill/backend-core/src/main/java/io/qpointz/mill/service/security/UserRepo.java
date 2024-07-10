package io.qpointz.mill.service.security;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRepo {

    @Getter
    @Setter
    private List<User> users;

}
