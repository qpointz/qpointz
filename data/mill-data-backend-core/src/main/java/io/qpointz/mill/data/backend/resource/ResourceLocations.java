package io.qpointz.mill.data.backend.resource;

/**
 * Helpers for Mill resource location strings shared by Spring adapters and Spring-free loaders.
 */
public final class ResourceLocations {

    private ResourceLocations() {}

    /**
     * Returns {@code true} when {@code location} uses an RFC 3986 style {@code scheme:} prefix that
     * should be resolved through a framework {@code ResourceLoader}, or {@code false} for bare
     * filesystem paths (including Windows absolute paths such as {@code C:\...}).
     *
     * @param location trimmed or untrimmed configuration location
     * @return whether the value carries a URI scheme
     */
    public static boolean hasUriScheme(String location) {
        if (location == null) {
            return false;
        }
        var colon = location.indexOf(':');
        if (colon <= 0) {
            return false;
        }
        if (colon == 1 && location.length() > 2 && Character.isLetter(location.charAt(0))
                && (location.charAt(2) == '\\' || location.charAt(2) == '/')) {
            return false;
        }
        for (var i = 0; i < colon; i++) {
            var c = location.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '+' && c != '-' && c != '.') {
                return false;
            }
        }
        return true;
    }
}
