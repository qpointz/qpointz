package io.qpointz.flow.io.matchers;

import io.qpointz.flow.io.BlobMatch;
import io.qpointz.flow.io.BlobMatcher;
import io.qpointz.flow.io.BlobPath;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class RegexBlobMatcher implements BlobMatcher {

    private final Pattern pattern;
    private final Map<Integer, String> groups;

    public RegexBlobMatcher(String pattern, Map<Integer,String> groupMetadata) {
        this.pattern = Pattern.compile(pattern);
        this.groups = groupMetadata != null
                ? groupMetadata
                : Map.of();
    }

    @Override
    public Optional<BlobMatch> match(BlobPath path) {
        val matcher = this.pattern.matcher(path.getUri().toString());

        if (!matcher.matches()) {
            return Optional.empty();
        }

        val meta = new HashMap<String,Object>();
        meta.put("blob-match", matcher.group());

        this.groups.entrySet().stream()
                .forEach(e -> meta.put(e.getValue(), matcher.group(e.getKey())));

        return Optional.of(new BlobMatch(path, meta));
    }
}
