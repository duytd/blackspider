package com.portia.analyzer;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * Created by duytd on 06/05/2015.
 */
public class PortiaAnalyzer extends Analyzer {

    protected CharArraySet stopWords = new CharArraySet(Version.LUCENE_30, 0, true);

    public PortiaAnalyzer(CharArraySet stopWords) {
        this.stopWords = stopWords;
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new PorterStemFilter(
                new StopFilter(Version.LUCENE_30,
                        new LowerCaseFilter(
                                new NumericFilter(
                                        new StandardFilter(
                                                new StandardTokenizer(Version.LUCENE_30, reader)))), stopWords));
    }
}