package io.qpointz.mill.service.security;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class User {

    private final Map<String,Object> details = new HashMap<>();

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private List<String> groups;

    private static class GroupAuthority implements GrantedAuthority {

        private final String group;

        public GroupAuthority(String role) {
            this.group = role;
        }

        @Override
        public String getAuthority() {
            return this.group;
        }
    }

    @JsonIgnore
    public List<GrantedAuthority> getAuthorities() {
        if (this.groups ==null) {
            return List.of();
        }

        return this.groups.stream()
                .distinct()
                .map(k-> (GrantedAuthority)(new GroupAuthority(k)))
                .toList();
    }

    @JsonAnySetter
    public void setDetails(String key, Object value) {
        details.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getDetails() {
        return details;
    }

}
