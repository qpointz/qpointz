package io.qpointz.mill.ai.chat.messages;

import com.google.common.base.Charsets;
import lombok.val;
import org.apache.commons.codec.digest.MurmurHash3;

public class HashUtils {

    public static int digest(String content) {
        val bytes = content.getBytes(Charsets.UTF_8);
        return MurmurHash3.hash32x86(bytes);
    }

}
