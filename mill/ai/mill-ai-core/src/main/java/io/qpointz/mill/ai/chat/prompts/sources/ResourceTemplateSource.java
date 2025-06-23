package io.qpointz.mill.ai.chat.prompts.sources;

import io.qpointz.mill.ai.chat.prompts.PromptTemplateSource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This class provides an implementation of the PromptTemplateSource interface.
 * It reads and returns the content of a given Resource object.
 */
public record ResourceTemplateSource(Resource resource) implements PromptTemplateSource {
    /*
     * Reads and returns the content of the resource as a string using
     * the specified charset.
     *
     * @param charset  the charset to use when reading the resource
     * @return The content of the resource as a String
     * @throws IOException  If an I/O error occurs
     */
    @Override
    public String content(Charset charset) throws IOException {
        return resource.getContentAsString(charset);
    }
}
