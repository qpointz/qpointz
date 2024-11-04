package io.qpointz.mill.security.authentication.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Builder
@Slf4j
public class EntraIdProfileLoader {

    private static final String AUTHORIZATION = "Authorization";
    private static final String SUFFIX = "Bearer";
    private static final String MS_GRAPH_API_ME_URL = "https://graph.microsoft.com/v1.0/me";
    private static final String MS_GRAPH_API_MEMBER_OF_URL = "https://graph.microsoft.com/v1.0/me/memberOf?$top=999&$select=displayName";
    private static final String USER_PRINCIPAL_NAME_KEY = "userPrincipalName";
    private static final String VALUE_KEY = "value";
    private static final String DISPLAY_NAME_KEY = "displayName";

    @Getter
    private final String bearerToken;

    public record EntraIdProfile(String principalName, List<GrantedAuthority> grantedAuthorities) {}

    public EntraIdProfile getProfile() {
        try {
            val principalName = getPrincipalInfo();
            val authorities = getAuthorities();
            return new EntraIdProfile(principalName, authorities);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private JsonNode execCall(String url) throws IOException {
        val client = new OkHttpClient();
        val request = new Request.Builder()
                .url(url)
                .addHeader(AUTHORIZATION, SUFFIX+" " + bearerToken)
                .build();
        try (val response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(String.format("Failed EntraId request. Url:%s Code:%s Message:%s Body:%s",
                        url, response.code(), response.message(),
                        response.body()!=null ? response.body().string() : ""));
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(response.body().string());
        }
    }

    private String getPrincipalInfo() throws IOException {
        val profile = execCall(MS_GRAPH_API_ME_URL);
        return profile.get(USER_PRINCIPAL_NAME_KEY).asText();
    }

    private List<GrantedAuthority> getAuthorities() throws IOException {
        val profile = execCall(MS_GRAPH_API_MEMBER_OF_URL);
        val authorities = new ArrayList<GrantedAuthority>();
        profile.withArray(VALUE_KEY)
                    .forEach(k-> authorities.add(new SimpleGrantedAuthority(k.get(DISPLAY_NAME_KEY).asText())));
        return authorities;
    }
}
