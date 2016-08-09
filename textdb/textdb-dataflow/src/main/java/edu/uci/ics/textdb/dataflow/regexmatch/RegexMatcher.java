package edu.uci.ics.textdb.dataflow.regexmatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import edu.uci.ics.textdb.api.common.IField;
import edu.uci.ics.textdb.api.common.IPredicate;
import edu.uci.ics.textdb.api.common.ITuple;
import edu.uci.ics.textdb.api.common.Schema;
import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.dataflow.ISourceOperator;
import edu.uci.ics.textdb.common.constants.DataConstants;
import edu.uci.ics.textdb.common.exception.DataFlowException;
import edu.uci.ics.textdb.common.exception.ErrorMessages;
import edu.uci.ics.textdb.common.field.DataTuple;
import edu.uci.ics.textdb.common.field.ListField;
import edu.uci.ics.textdb.common.field.Span;
import edu.uci.ics.textdb.common.utils.Utils;
import edu.uci.ics.textdb.dataflow.common.RegexPredicate;
import edu.uci.ics.textdb.dataflow.source.IndexBasedSourceOperator;
import edu.uci.ics.textdb.storage.DataReaderPredicate;

/**
 * Created by chenli on 3/25/16.
 * @author laishuying
 */
public class RegexMatcher implements IOperator {
    private RegexPredicate regexPredicate;
    
    private String regex;
    private List<String> fieldNameList;
    
    private Schema sourceTupleSchema;
    private Schema spanSchema;
    
	private IOperator inputOperator;
    
	private int limit;
	private int cursor;
	private int offset;
        
    // two available regex engines, RegexMatcher will try RE2J first 
	private enum RegexEngine {
		JavaRegex,
		RE2J
	}
	private RegexEngine regexEngine;
	private com.google.re2j.Pattern re2jPattern;
	private java.util.regex.Pattern javaPattern;
	
	
    public RegexMatcher(IPredicate predicate) throws DataFlowException{
    	this.cursor = -1;
    	this.offset = 0;
    	this.limit = Integer.MAX_VALUE;
    	this.regexPredicate = (RegexPredicate) predicate;
    	this.regex = regexPredicate.getRegex();
    	this.fieldNameList = regexPredicate.getFieldNameList();
    			
		// try Java Regex first
		try {
			this.javaPattern = java.util.regex.Pattern.compile(regex);
			this.regexEngine = RegexEngine.JavaRegex;
		// if Java Regex fails, try RE2J
		} catch (java.util.regex.PatternSyntaxException javaException) {
	    	try {
	    		this.re2jPattern = com.google.re2j.Pattern.compile(regexPredicate.getRegex());
				this.regexEngine = RegexEngine.RE2J;
			// if RE2J also fails, throw exception
	    	} catch (com.google.re2j.PatternSyntaxException re2jException) {
				throw new DataFlowException(javaException.getMessage(), javaException);
	    	}
		}		
    }
	
	
    @Override
    public ITuple getNextTuple() throws DataFlowException {
		try {
			if (limit == 0 || cursor >= offset + limit - 1){
				return null;
			}
			ITuple sourceTuple;
			ITuple resultTuple = null;
			while ((sourceTuple = inputOperator.getNextTuple()) != null) {     
	            resultTuple = computeNextTuple(sourceTuple);
	            
	            if (resultTuple != null) {
		            cursor++;
	            }
	            if (cursor >= offset){
	            	break;
	            }
			}
			return resultTuple;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFlowException(e.getMessage(), e);
        }        
    }
    
    public void setLimit(int limit){
    	this.limit = limit;
    }
    
    public int getLimit(){
    	return this.limit;
    }
    
    public void setOffset(int offset){
    	this.offset = offset;
    }
    
    public int getOffset(){
    	return this.offset;
    }
    
    private ITuple constructSpanTuple(List<IField> fields, List<Span> spans) {
    	List<IField> fieldListDuplicate = new ArrayList<>(fields);
    	IField spanListField = new ListField<Span>(spans);
    	fieldListDuplicate.add(spanListField);
    	IField[]  fieldsDuplicate = fieldListDuplicate.toArray(new IField[fieldListDuplicate.size()]);
    	return new DataTuple(spanSchema, fieldsDuplicate);
    }
	
    
	/**
	 * This function returns a list of spans in the given tuple that match the
	 * regex For example, given tuple ("george watson", "graduate student", 23,
	 * "(949)888-8888") and regex "g[^\s]*", this function will return
	 * [Span(name, 0, 6, "g[^\s]*", "george
	 * watson"), Span(position, 0, 8, "g[^\s]*", "graduate student")]
	 * 
	 * @param tuple
	 *            document in which search is performed
	 * @return a list of spans describing the occurrence of a matching sequence
	 *         in the document
	 */
	public ITuple computeNextTuple(ITuple tuple) {
		List<Span> spanList = new ArrayList<>();
		if (tuple == null) {
			return null;
		}
		for (String fieldName : fieldNameList) {
			IField field = tuple.getField(fieldName);
			String fieldValue = field.getValue().toString();
			if (fieldValue == null) {
				return null;
			} else {
				switch (regexEngine) {
				case JavaRegex:
					spanList = javaRegexMatch(fieldValue, fieldName, spanList);
					break;
				case RE2J:
					spanList = re2jRegexMatch(fieldValue, fieldName, spanList);
					break;
				}
			}
		}
		if (spanList.isEmpty()) {
			return null;
		}
		return constructSpanTuple(tuple.getFields(),spanList);
	}
	
	
	private List<Span> javaRegexMatch(String fieldValue, String fieldName, List<Span> spanList) {
		java.util.regex.Matcher javaMatcher = this.javaPattern.matcher(fieldValue);
		while (javaMatcher.find()) {
			int start = javaMatcher.start();
			int end = javaMatcher.end();
			spanList.add(new Span(fieldName, start, end, 
					this.regexPredicate.getRegex(), fieldValue.substring(start, end)));
		}
		return spanList;
	}
	
	private List<Span> re2jRegexMatch(String fieldValue, String fieldName, List<Span> spanList) {
		com.google.re2j.Matcher re2jMatcher = this.re2jPattern.matcher(fieldValue);
		while (re2jMatcher.find()) {
			int start = re2jMatcher.start();
			int end = re2jMatcher.end();
			spanList.add(new Span(fieldName, start, end, 
					this.regexPredicate.getRegex(), fieldValue.substring(start, end)));
		}
		return spanList;
	}
	
	/**
	 * Use Java's built-in Regex Engine. <br>
	 * RegexMatcher is set to use Java Regex Engine by default. <br>
	 * @throws java.util.regex.PatternSyntaxException
	 */
	public void setRegexEngineToJava() throws java.util.regex.PatternSyntaxException {
		if (this.regexEngine == RegexEngine.JavaRegex) {
			return;
		} else {
			this.javaPattern = java.util.regex.Pattern.compile(this.regex);
			this.regexEngine = RegexEngine.JavaRegex;
		}
	}

	/**
	 * Use RE2J Regex Engine. <br>
	 * RegexMatcher is set to use Java Regex Engine by default. 
	 * Because Java Regex is usually faster than RE2J <br>
	 * @throws java.util.regex.PatternSyntaxException
	 */
	public void setRegexEngineToRE2J() throws java.util.regex.PatternSyntaxException {
		if (this.regexEngine == RegexEngine.RE2J) {
			return;
		} else {
			try {
				this.re2jPattern = com.google.re2j.Pattern.compile(this.regex);
				this.regexEngine = RegexEngine.RE2J;
			} catch (com.google.re2j.PatternSyntaxException e) {
				throw new java.util.regex.PatternSyntaxException(e.getDescription(), e.getPattern(), e.getIndex());
			}
		}
	}
	
	public String getRegexEngineString() {
		return this.regexEngine.toString();
	}
    
    
    @Override
    public void open() throws DataFlowException {
        if (this.inputOperator == null) {
            throw new DataFlowException(ErrorMessages.INPUT_OPERATOR_NOT_SPECIFIED);
        }
        
        try {
            inputOperator.open();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFlowException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws DataFlowException {
        try {
            if (inputOperator != null) {
                inputOperator.close();   
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFlowException(e.getMessage(), e);
        }
    }
    

    public Schema getSpanSchema() {
    	return spanSchema;
    }
    
    public String getRegex() {
    	return this.regex;
    }
    
    public IOperator getInputOperator() {
		return inputOperator;
	}

	public void setInputOperator(ISourceOperator inputOperator) {
		this.inputOperator = inputOperator;
	}
}
