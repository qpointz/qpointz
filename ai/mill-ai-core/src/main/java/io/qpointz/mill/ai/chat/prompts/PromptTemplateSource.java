package io.qpointz.mill.ai.chat.prompts;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * PromptTemplateSource interface provides methods for a class that implements reading templates content.
 */
public interface PromptTemplateSource  {

    /*
     * Reads the template content using specified charset.
     *
     * @param charset  the charset to use when reading the content
     * @return the template content as a String
     * @throws IOException if an I/O error occurs when reading the content
     */
    String content(Charset charset) throws IOException;

    /**
     * Reads the template content using UTF-8 charset.
     *
     * @return the template content as a String
     * @throws IOException if an I/O error occurs when reading the content
     */
    default String content() throws IOException {
        return content(StandardCharsets.UTF_8);
    }
}
