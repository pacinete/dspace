package org.dspace.app.cris.importexport;

import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class CSVBulkChanges implements IBulkChanges {
	
    private static Logger log = Logger.getLogger(CSVBulkChanges.class);
	
	static final String HEADER_SOURCEID = "SOURCEID";

	static final String HEADER_SOURCEREF = "SOURCEREF";

	static final String HEADER_UUID = "UUID";

	static final String HEADER_CRISID = "CRISID";

	static final String HEADER_ACTION = "action";

	final static String[] HEADER_COLUMNS = new String[]{HEADER_ACTION,HEADER_CRISID,HEADER_UUID,HEADER_SOURCEREF,HEADER_SOURCEID};
	
	private Sheet mainObjects;
	private Sheet nestedObjects;
	
	private List<String> mainHeaders = new ArrayList<String>();
	private List<String> nestedHeaders = new ArrayList<String>();

	public CSVBulkChanges(Workbook workbook) {
        Cell[] row;
        this.mainObjects = workbook.getSheet("main_entities");
        this.nestedObjects = workbook.getSheet("nested_entities");
        int column = 0;
        row = mainObjects.getRow(0);
        while (column<row.length)
        {
        	String cellContent = StringUtils.trim(row[column].getContents());
        	if (StringUtils.isNotBlank(cellContent)) {
				mainHeaders.add(cellContent);
        		if (HEADER_COLUMNS.length > column && !StringUtils.equalsIgnoreCase(cellContent, HEADER_COLUMNS[column])) {
        			throw new IllegalArgumentException("Invalid excel file[main_entities sheet] - unexpected header column " + column+ " -> " + cellContent + " expected "+HEADER_COLUMNS[column]);		
        		}
        		column++;
        	}        	
        }
        
        if (mainHeaders.size() < HEADER_COLUMNS.length) {
        	throw new IllegalArgumentException("Invalid excel file[main_entities sheet] - unexpected header row: missing the required first 5 cells (action, CRISID, UUID, SOURCEREF, SOURCEID)");
        }
        
        if (nestedObjects != null) {
	        column = 0;
	        row = nestedObjects.getRow(0);
	        while (column<row.length)
	        {
	        	String cellContent = StringUtils.trim(row[column].getContents());
	        	if (StringUtils.isNotBlank(cellContent)) {
	        		nestedHeaders.add(cellContent);
	        		if (HEADER_COLUMNS.length > column && !StringUtils.equalsIgnoreCase(cellContent, HEADER_COLUMNS[column])) {
	        			throw new IllegalArgumentException("Invalid excel file[nested_entities sheet] - unexpected header column " + column+ " -> " + cellContent + " expected "+HEADER_COLUMNS[column]);		
	        		}
	        		column++;
	        	}	        	
	        }
	        
	        if (nestedHeaders.size() < HEADER_COLUMNS.length) {
	        	throw new IllegalArgumentException("Invalid excel file[nested_entities sheet] - unexpected header row: missing the required first 5 cells (action, CRISID, UUID, SOURCEREF, SOURCEID)");
	        }
        }
	}

	@Override
	public boolean hasPropertyDefinition(String shortName) {
		return mainHeaders.contains(shortName) || (nestedHeaders != null && nestedHeaders.contains(shortName));
	}

	@Override
	public int size() {
		return mainObjects.getRows() -1 + (nestedObjects != null?nestedObjects.getRows()-1:0);
	}

	@Override
	public IBulkChange getChanges(int i) {
		if (i < mainObjects.getRows()) {
			log.debug("Retrieve in entity sheet row #"+i);
			return new CSVBulkChange(mainObjects.getRow(i), mainHeaders);
		}
		else {
			log.debug("Retrieve in nested sheet row #"+ (i+1));
			return new CSVBulkChange(nestedObjects.getRow((i - (mainObjects.getRows() -1))+1),nestedHeaders);	
		}
	}

}
