package edu.uci.ics.textdb.dataflow.nlpextrator;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


import edu.uci.ics.textdb.api.common.Attribute;
import edu.uci.ics.textdb.api.common.IField;
import edu.uci.ics.textdb.api.common.ITuple;
import edu.uci.ics.textdb.api.common.Schema;
import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.common.exception.DataFlowException;
import edu.uci.ics.textdb.common.field.Span;
import edu.uci.ics.textdb.common.utils.Utils;

import java.util.*;


/**
 * @author Feng
 * @about Wrap the Stanford NLP as an operator to extractor desired
 * information (Named Entities, Part of Speech).
 * This operator could recognize 7 Named Entity classes: Location,
 * Person, Organization, Money, Percent, Date and Time.
 * It'll also detect 4 types of Part of Speech: Noun, Verb, Adjective
 * and Adverb.
 * The function will return the extracted tokens as a list of spans and
 * appends to the original tuple as a new field.
 * For example: Given tuple with two fields: sentence1 (string), sentence2(string),
 * specify to extract all Named Entities.
 * Source Tuple: ["Google is an organization.", "Its headquarters are in
 * Mountain View."]
 * Appends a list of spans as a field for the returned NlpExtractor tuple with extracted StanfordNLP token type:
 * ["sentence1,0,6,Google, Organization", "sentence2,24,37,Mountain View,
 * Location"]
 */

public class NlpExtractor implements IOperator {


    private IOperator sourceOperator;
    private List<Attribute> searchInAttributes;
    private ITuple sourceTuple;
    private Schema returnSchema;
    private NlpTokenType inputNlpTokenType = null;
    private String flag = null;


    /**
     * Named Entity token type: NE_ALL, Number, Location, Person,
     * Organization, Money, Percent, Date, Time.
     * Part Of Speech token type: Noun, Verb, Adjective, Adverb
     */
    public enum NlpTokenType {
        NE_ALL, Number, Location, Person, Organization, Money, Percent,
        Date, Time, Noun, Verb, Adjective, Adverb;

        private static boolean isPOSTokenType(NlpTokenType token) {
            if (token.equals(NlpTokenType.Adjective) ||
                    token.equals(NlpTokenType.Adverb) ||
                    token.equals(NlpTokenType.Noun) ||
                    token.equals(NlpTokenType.Verb)) {
                return true;
            } else {
                return false;
            }
        }
    }

    ;


    /**
     * @param operator
     * @param searchInAttributes
     * @param inputNlpTokenType
     * @throws DataFlowException
     * @about The constructor of the NlpExtractor. Allow users to pass
     * a list of attributes and an inputNlpTokenType.
     * The operator will only search within the attributes and return
     * the same tokens that are recognized as the same input
     * inputNlpTokenType. If the input token is NlpTokenType.NE_ALL,
     * return all tokens that are recognized as NamedEntity token.
     */
    public NlpExtractor(IOperator operator, List<Attribute>
            searchInAttributes, NlpTokenType inputNlpTokenType)
            throws DataFlowException {
        this.sourceOperator = operator;
        this.searchInAttributes = searchInAttributes;
        this.inputNlpTokenType = inputNlpTokenType;
        if (NlpTokenType.isPOSTokenType(inputNlpTokenType)) {
            flag = "POS";
        } else {
            flag = "NE_ALL";
        }
    }


    @Override
    public void open() throws Exception {
        try {
            sourceOperator.open();
            returnSchema = null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFlowException(e.getMessage(), e);
        }
    }


    /**
     * @about Extract a list of spans based on the input token.
     * Append the list as a new field to the original tuple and return.
     * @overview Get a tuple from the source operator
     * Use the Stanford NLP package to process specified fields.
     * For all recognized tokens that match the input token,
     * create their spans and make them as a list. Append the list
     * as a field in the original tuple.
     */
    @Override
    public ITuple getNextTuple() throws Exception {
        sourceTuple = sourceOperator.getNextTuple();
        if (sourceTuple == null) {
            return null;
        } else {
            if (returnSchema == null) {
                returnSchema = Utils.createSpanSchema(sourceTuple.getSchema());
            }
            List<Span> spanList = new ArrayList<>();
            for (Attribute attribute : searchInAttributes) {
                String fieldName = attribute.getFieldName();
                IField field = sourceTuple.getField(fieldName);
                spanList.addAll(extractNlpSpans(field, fieldName));
            }
            ITuple returnTuple = Utils.getSpanTuple(sourceTuple.getFields(),
                    spanList, returnSchema);
            sourceTuple = sourceOperator.getNextTuple();
            return returnTuple;
        }
    }

    /**
     * @param iField
     * @param fieldName
     * @return
     * @about This function takes an IField(TextField) and a String
     * (the field's name) as input and uses the Stanford NLP package
     * to process the field based on the input token and flag.
     * In the result spans, value represents the word itself
     * and key represents the recognized token
     * @overview First set up a pipeline of Annotators based on the flag.
     * If the flag is "NE_ALL", we set up the NamedEntityTagAnnotator,
     * if it's "POS", then only PartOfSpeechAnnotator is needed.
     *
     * The pipeline has to be this order: TokenizerAnnotator,
     * SentencesAnnotator, PartOfSpeechAnnotator, LemmaAnnotator and
     * NamedEntityTagAnnotator.
     *
     * In the pipeline, each token is wrapped as a CoreLabel
     * and each sentence is wrapped as CoreMap. Each annotator adds its
     * annotation to the CoreMap(sentence) or CoreLabel(token) object.
     *
     * After the pipeline, scan each CoreLabel(token) for its
     * NamedEntityAnnotation or PartOfSpeechAnnotator depends on the flag
     *
     * For each Stanford NLP annotation, get it's corresponding inputNlpTokenType
     * that used in this package, then check if it equals to the input token.
     * If yes, makes it a span and add to the return list.
     *
     * The NLP package has annotations for the start and end position of a token
     * and it perfectly matches the span design so we just use them.
     *
     * For Example: With TextField value: "Microsoft, Google and Facebook are
     * organizations while Donald Trump and Barack Obama are persons", with
     * fieldName: Sentence1 and inputToken is Organization. Since the
     * inputToken require us to use NamedEntity Annotator in the Stanford
     * NLP package, the flag would be set to "NE".
     * The pipeline would set up to cover the Named Entity Recognizer. Then
     * get the value of NamedEntityTagAnnotation for each CoreLabel(token).If
     * the value is the token "Organization", then it meets the
     * requirement. In this case "Microsoft","Google" and "Facebook" will
     * satisfy the requirement. "Donald Trump" and "Barack Obama" would
     * have token "Person" and do not meet the requirement. For each
     * qualified token, create a span accordingly and add it to the returned
     * list. In this case, token "Microsoft" would be span:
     * ["Sentence1", 0, 9, Organization, "Microsoft"]
     */
    private List<Span> extractNlpSpans(IField iField, String fieldName) {
        List<Span> spanList = new ArrayList<>();
        String text = (String) iField.getValue();
        Properties props = new Properties();

        //Setup Stanford NLP pipelien based on flag
        if (flag.equals("POS")) {
            props.setProperty("annotators", "tokenize, ssplit, pos");
        } else {
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, " +
                    "ner");
        }
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation documentAnnotation = new Annotation(text);
        pipeline.annotate(documentAnnotation);
        List<CoreMap> sentences = documentAnnotation.get(CoreAnnotations.
                SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations
                    .TokensAnnotation.class)) {

                String stanfordNlpToken;

                //Extract annotations based on flag
                if (flag.equals("POS")) {
                    stanfordNlpToken = token.get(CoreAnnotations
                            .PartOfSpeechAnnotation.class);
                } else {
                    stanfordNlpToken = token.get(CoreAnnotations
                            .NamedEntityTagAnnotation.class);
                }


                NlpTokenType thisNlpTokenType = getNlpTokenType
                        (stanfordNlpToken);
                if (thisNlpTokenType == null) {
                    continue;
                }
                if (inputNlpTokenType.equals(NlpTokenType.NE_ALL) ||
                        inputNlpTokenType.equals(thisNlpTokenType)) {
                    int start = token.get(CoreAnnotations
                            .CharacterOffsetBeginAnnotation.class);
                    int end = token.get(CoreAnnotations
                            .CharacterOffsetEndAnnotation.class);
                    String word = token.get(CoreAnnotations.TextAnnotation
                            .class);

                    Span span = new Span(fieldName, start, end,
                            thisNlpTokenType.toString(), word);
                    if (spanList.size() >= 1 && (flag.equals("NE_ALL"))) {
                        Span previousSpan = spanList.get(spanList.size() - 1);
                        if (previousSpan.getFieldName().equals(span
                                .getFieldName())
                                && (span.getStart() - previousSpan.getEnd() <= 1)
                                && previousSpan.getKey().equals(span.getKey())) {
                            Span newSpan = mergeTwoSpans(previousSpan, span);
                            span = newSpan;
                            spanList.remove(spanList.size() - 1);
                        }
                    }
                    spanList.add(span);
                }
            }
        }
        return spanList;
    }


    /**
     * @param previousSpan
     * @param currentSpan
     * @return
     * @about This function takes two spans as input and merges them as a
     * new span
     *
     * Two spans with fieldName, start, end, key, value:
     * previousSpan: "Doc1", 10, 13, "Location", "New"
     * currentSpan : "Doc1", 14, 18, "Location", "York"
     *
     * Would be merge to:
     * return:   "Doc1", 10, 18, "Location", "New York"
     *
     * The caller needs to make sure:
     * 1. The two spans are adjacent.
     * 2. The two spans are in the same field. They should have the same
     * fieldName.
     * 3. The two spans have the same key (Organization, Person,... etc)
     */
    private Span mergeTwoSpans(Span previousSpan, Span currentSpan) {
        String newWord = previousSpan.getValue() + " " + currentSpan.getValue();
        return new Span(previousSpan.getFieldName(), previousSpan.getStart()
                , currentSpan.getEnd(), previousSpan.getKey(), newWord);
    }


    /**
     * @param stanfordTokenType
     * @return
     * @about This function takes a Stanford NLP token (Named Entity 7
     * classes: LOCATION,PERSON,ORGANIZATION,MONEY,PERCENT,DATE,
     * TIME and NUMBER and Part of Speech tokens) and returns the
     * corresponding enum NlpTokenType.
     * For Part of Speech, we match all Stanford POStoken to only 4 types:
     * Noun, Verb, Adjective and Adverb.
     */
    private NlpTokenType getNlpTokenType(String stanfordTokenType) {
        switch (stanfordTokenType) {
            case "NUMBER":
                return NlpTokenType.Number;
            case "LOCATION":
                return NlpTokenType.Location;
            case "PERSON":
                return NlpTokenType.Person;
            case "ORGANIZATION":
                return NlpTokenType.Organization;
            case "MONEY":
                return NlpTokenType.Money;
            case "PERCENT":
                return NlpTokenType.Percent;
            case "DATE":
                return NlpTokenType.Date;
            case "TIME":
                return NlpTokenType.Time;
            case "JJ":
                return NlpTokenType.Adjective;
            case "JJR":
                return NlpTokenType.Adjective;
            case "JJS":
                return NlpTokenType.Adjective;
            case "RB":
                return NlpTokenType.Adverb;
            case "RBR":
                return NlpTokenType.Adverb;
            case "RBS":
                return NlpTokenType.Adverb;
            case "NN":
                return NlpTokenType.Noun;
            case "NNS":
                return NlpTokenType.Noun;
            case "NNP":
                return NlpTokenType.Noun;
            case "NNPS":
                return NlpTokenType.Noun;
            case "VB":
                return NlpTokenType.Verb;
            case "VBD":
                return NlpTokenType.Verb;
            case "VBG":
                return NlpTokenType.Verb;
            case "VBN":
                return NlpTokenType.Verb;
            case "VBP":
                return NlpTokenType.Verb;
            case "VBZ":
                return NlpTokenType.Verb;
            default:
                return null;
        }
    }


    @Override
    public void close() throws DataFlowException {
        try {
            inputNlpTokenType = null;
            searchInAttributes = null;
            sourceTuple = null;
            returnSchema = null;
            sourceOperator.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFlowException(e.getMessage(), e);
        }
    }
}
