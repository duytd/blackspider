package com.portia.analyzer;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * Created by duytd on 06/05/2015.
 */
public class NumericFilter extends TokenFilter {
    public NumericFilter(TokenStream tokenStream) {
        super(tokenStream);
    }

    protected CharTermAttribute charTermAttribute =
            addAttribute(CharTermAttribute.class);
    protected PositionIncrementAttribute positionIncrementAttribute =
            addAttribute(PositionIncrementAttribute.class);

    @Override
    public boolean incrementToken() throws IOException {
        String nextToken = null;
        while (nextToken == null) {

            // End of the token stream
            if ( ! this.input.incrementToken()) {
                return false;
            }

            String currentTokenInStream =
                    this.input.getAttribute(CharTermAttribute.class)
                            .toString().trim();

            // Save the token if it is not a number
            if (!NumberUtils.isNumber(currentTokenInStream)) {
                nextToken = currentTokenInStream;
            }
        }

        // Save the current token
        this.charTermAttribute.setEmpty().append(nextToken);
        this.positionIncrementAttribute.setPositionIncrement(1);
        return true;
    }
}
