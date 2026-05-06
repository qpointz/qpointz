package io.qpointz.mill.export;

/**
 * Builds safe attachment filenames for export responses.
 */
public final class ExportFilenameSanitizer {

    private ExportFilenameSanitizer() {
    }

    /**
     * @param requested user-supplied name (may be null)
     * @param extension fallback extension without dot
     * @return sanitized filename containing no path separators
     */
    public static String sanitize(String requested, String extension) {
        String base = requested != null && !requested.isBlank() ? requested.trim() : "export." + extension;
        base = base.replace('\\', '_').replace('/', '_').replace("..", "_");
        if (!base.toLowerCase().endsWith("." + extension.toLowerCase())) {
            if (!base.endsWith(".")) {
                base = base + "." + extension;
            } else {
                base = base + extension;
            }
        }
        return base;
    }
}
