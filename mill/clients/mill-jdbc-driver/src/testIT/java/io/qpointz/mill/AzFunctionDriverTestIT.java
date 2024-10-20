package io.qpointz.mill;

import lombok.val;

public class AzFunctionDriverTestIT extends BaseDriverTestIT {
    @Override
    String getConnectionUrl() {
        val ci = envOr("CI", "false");
        val defaultUrl = ci.equals("false")
                ? "http://localhost:7071/api/"
                : null;
        val apiUrl = envOr("MILL_AZ_FUNC_API_URL", defaultUrl);

        return apiUrl == null || apiUrl.isEmpty()
                ? null
                : String.format("jdbc:mill:%s", apiUrl);
    }
}
