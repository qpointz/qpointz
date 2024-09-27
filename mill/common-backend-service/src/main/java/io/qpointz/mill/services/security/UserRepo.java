package io.qpointz.mill.services.security;

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
