package io.qpointz.mill.security.authorization.policy.matcher;

import java.util.List;

public class PolicyMatcher {

    public boolean matchesTable(List<String> pattern, List<String> tableName) {
        if (pattern == null || tableName == null) {
            return false;
        }
        if (pattern.size() != tableName.size()) {
            return false;
        }
        for (int i = 0; i < pattern.size(); i++) {
            if (!matchSegment(pattern.get(i), tableName.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesColumn(String pattern, String columnName) {
        if (pattern == null || columnName == null) {
            return false;
        }
        return matchSegment(pattern, columnName);
    }

    private boolean matchSegment(String pattern, String value) {
        return globMatch(pattern.toLowerCase(), value.toLowerCase());
    }

    private boolean globMatch(String pattern, String text) {
        int pi = 0, ti = 0;
        int starIdx = -1, matchIdx = 0;

        while (ti < text.length()) {
            if (pi < pattern.length() && (pattern.charAt(pi) == text.charAt(ti))) {
                pi++;
                ti++;
            } else if (pi < pattern.length() && pattern.charAt(pi) == '*') {
                starIdx = pi;
                matchIdx = ti;
                pi++;
            } else if (starIdx != -1) {
                pi = starIdx + 1;
                matchIdx++;
                ti = matchIdx;
            } else {
                return false;
            }
        }

        while (pi < pattern.length() && pattern.charAt(pi) == '*') {
            pi++;
        }

        return pi == pattern.length();
    }
}
