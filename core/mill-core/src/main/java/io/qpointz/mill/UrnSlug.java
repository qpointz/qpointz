package io.qpointz.mill;

/**
 * Bijective, URL-safe encoding of Mill URN keys for REST path segments.
 *
 * <p>URN strings (e.g. {@code urn:mill/metadata/scope:global}) are the authoritative keys used
 * in storage, response bodies, and query parameters. They cannot be placed in URL <em>path
 * segments</em> as-is because the forward slash ({@code /}) is a path separator.
 *
 * <p>Two slug modes are supported:
 *
 * <h2>Mode 1 — Full slug (generic)</h2>
 *
 * <p>Encodes the entire URN when there is no fixed namespace for the path segment.
 *
 * <pre>{@code
 * Encode:
 *   urn:mill/metadata/facet-type:descriptive
 *   → drop "urn:"          →  mill/metadata/facet-type:descriptive
 *   → replace "-" → "--"   →  mill/metadata/facet--type:descriptive
 *   → replace "/" → "-"    →  mill-metadata-facet--type:descriptive
 *
 * Decode (reverse):
 *   mill-metadata-facet--type:descriptive
 *   → replace "--" with sentinel
 *   → replace "-" → "/"
 *   → restore sentinel → "-"
 *   → prepend "urn:"        →  urn:mill/metadata/facet-type:descriptive
 * }</pre>
 *
 * <h2>Mode 2 — Prefixed slug (namespace-scoped, preferred)</h2>
 *
 * <p>When the controller owns a single URN namespace, the common prefix is stripped and only
 * the local identifier is used as the path segment. Colons ({@code :}) survive unchanged.
 *
 * <pre>{@code
 * prefix = "urn:mill/metadata/scope:"
 *
 * urn:mill/metadata/scope:global      →  global
 * urn:mill/metadata/scope:user:alice  →  user:alice
 * urn:mill/metadata/scope:team:eng    →  team:eng
 * }</pre>
 */
public final class UrnSlug {

    private static final String URN_PREFIX = "urn:";
    /**
     * Sentinel placeholder used internally during encoding/decoding to preserve literal hyphens.
     * Must not appear in any valid URN or slug.
     */
    private static final String SENTINEL = "\u0000";

    private UrnSlug() {
    }

    // ── Mode 1: Full slug ────────────────────────────────────────────────

    /**
     * Encodes a full URN to a URL-safe slug.
     *
     * <p>Algorithm: drop the {@code "urn:"} prefix, escape literal {@code "-"} to {@code "--"},
     * then replace {@code "/"} with {@code "-"}.
     *
     * @param urn the full URN to encode (must start with {@code "urn:"})
     * @return URL-safe slug string
     * @throws IllegalArgumentException if {@code urn} does not start with {@code "urn:"}
     */
    public static String encode(String urn) {
        if (!urn.startsWith(URN_PREFIX)) {
            throw new IllegalArgumentException("Not a URN: " + urn);
        }
        String stripped = urn.substring(URN_PREFIX.length()); // drop "urn:"
        String escaped  = stripped.replace("-", "--");         // escape literal hyphens
        return escaped.replace("/", "-");                      // "/" → "-"
    }

    /**
     * Decodes a full slug back to its URN.
     *
     * <p>Algorithm: replace {@code "-"} with {@code "/"} (guarded by sentinel for {@code "--"}),
     * then prepend {@code "urn:"}.
     *
     * @param slug the slug to decode (as produced by {@link #encode(String)})
     * @return full URN string starting with {@code "urn:"}
     * @throws IllegalArgumentException if the slug is malformed
     */
    public static String decode(String slug) {
        // Use sentinel to protect escaped "--" (represents a literal "-")
        String withSentinel = slug.replace("--", SENTINEL);
        String slashed      = withSentinel.replace("-", "/");
        String restored     = slashed.replace(SENTINEL, "-");
        return URN_PREFIX + restored;
    }

    // ── Mode 2: Prefixed slug ────────────────────────────────────────────

    /**
     * Encodes a URN to a prefixed slug by stripping {@code urnPrefix}.
     *
     * <p>Example: {@code encode("urn:mill/metadata/scope:global", "urn:mill/metadata/scope:")}
     * returns {@code "global"}.
     *
     * @param urn       the full URN to encode
     * @param urnPrefix the namespace prefix to strip
     * @return the local part of the URN after the prefix
     * @throws IllegalArgumentException if {@code urn} does not start with {@code urnPrefix}
     */
    public static String encode(String urn, String urnPrefix) {
        if (!urn.startsWith(urnPrefix)) {
            throw new IllegalArgumentException(
                    "URN \"" + urn + "\" does not belong to namespace \"" + urnPrefix + "\"");
        }
        return urn.substring(urnPrefix.length());
    }

    /**
     * Decodes a prefixed slug back to its full URN by prepending {@code urnPrefix}.
     *
     * <p>Validates that the resulting URN starts with {@code urnPrefix}; throws
     * {@link IllegalArgumentException} if not (e.g. if the client passes a scope slug to a
     * facet endpoint).
     *
     * <p>Example: {@code decode("user:alice", "urn:mill/metadata/scope:")} returns
     * {@code "urn:mill/metadata/scope:user:alice"}.
     *
     * @param slug      the prefixed slug (local part)
     * @param urnPrefix the namespace prefix to prepend
     * @return full URN string
     * @throws IllegalArgumentException if the resulting URN does not start with {@code urnPrefix}
     */
    public static String decode(String slug, String urnPrefix) {
        String urn = urnPrefix + slug;
        if (!urn.startsWith(urnPrefix)) {
            throw new IllegalArgumentException(
                    "Decoded URN \"" + urn + "\" does not belong to namespace \"" + urnPrefix + "\"");
        }
        return urn;
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the value is a full URN (starts with {@code "urn:"}).
     *
     * @param value the string to test
     * @return {@code true} if {@code value} starts with {@code "urn:"}
     */
    public static boolean isUrn(String value) {
        return value.startsWith(URN_PREFIX);
    }

    /**
     * Normalises a path variable that may be a full URN, a prefixed slug, or a legacy short key.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>If {@code value} starts with {@code "urn:"}: validate it belongs to {@code urnPrefix};
     *       return as-is.</li>
     *   <li>Otherwise: attempt to decode as a prefixed slug
     *       ({@link #decode(String, String)}). If that fails, fall back to
     *       {@code shortKeyExpander}.</li>
     * </ol>
     *
     * @param value           path variable value (URN, prefixed slug, or legacy short key)
     * @param urnPrefix       expected URN namespace prefix
     * @param shortKeyExpander function that maps legacy short keys to full URNs;
     *                         should throw {@link IllegalArgumentException} for unknown keys
     * @return normalised full URN
     * @throws IllegalArgumentException if the value cannot be resolved
     */
    public static String normalise(String value, String urnPrefix,
                                    java.util.function.UnaryOperator<String> shortKeyExpander) {
        if (isUrn(value)) {
            if (!value.startsWith(urnPrefix)) {
                throw new IllegalArgumentException(
                        "URN \"" + value + "\" not in namespace \"" + urnPrefix + "\"");
            }
            return value;
        }
        try {
            return decode(value, urnPrefix);
        } catch (IllegalArgumentException ignored) {
            return shortKeyExpander.apply(value);
        }
    }

    /**
     * Normalises a path variable that may be a full URN or a prefixed slug
     * (no legacy short key support).
     *
     * @param value     path variable value (URN or prefixed slug)
     * @param urnPrefix expected URN namespace prefix
     * @return normalised full URN
     * @throws IllegalArgumentException if the value cannot be resolved
     */
    public static String normalise(String value, String urnPrefix) {
        return normalise(value, urnPrefix, v -> {
            throw new IllegalArgumentException("Unknown key: " + v);
        });
    }
}
