package io.qpointz.mill.ai.chat.prompts.sources;

import io.qpointz.mill.ai.chat.prompts.PromptTemplateSource;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A {@link PromptTemplateSource} implementation that wraps a raw string as a template content source.
 * <p>
 * Useful for cases where template content is constructed or supplied directly in code, rather than loaded from
 * an external file or resource.
 * </p>
 *
 * @param content The string content of the template.
 */
public record StringTemplateSource(String content) implements PromptTemplateSource {

    /**
     * Returns the template content as a string. Ignores the specified charset since the content is already a string.
     *
     * @param charset The character set (ignored).
     * @return The template content as a string.
     * @throws IOException Never thrown in this implementation.
     */
    @Override
    public String content(Charset charset) throws IOException {
        return content;
    }
}
