package Implementation.Tokenizer;

import java.nio.charset.Charset;

/**
 * Created by Tal on 15/01/2016.
 */
public class FixedSeparatorMessageTokenizerFactory implements TokenizerFactory<StringMessage> {
    @Override
    public MessageTokenizer<StringMessage> create() {
        return new FixedSeparatorMessageTokenizer("\n", Charset.forName("UTF-8"));
    }
}
