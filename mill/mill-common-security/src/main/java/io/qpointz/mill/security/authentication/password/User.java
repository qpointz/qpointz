package io.qpointz.mill.security.authentication.password;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class User implements UserDetails {

    private final Map<String,Object> details = new HashMap<>();

    @Getter
    @Setter
    @JsonProperty("name")
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

    @Override
    public String getUsername() {
        return this.getName();
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
