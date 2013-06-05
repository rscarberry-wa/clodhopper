package org.battelle.clodhopper.tuple;

import org.battelle.clodhopper.task.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;

/*=====================================================================
 * 
 *                       CLODHOPPER CLUSTERING API
 * 
 * -------------------------------------------------------------------- 
 * 
 * Copyright (C) 2013 Battelle Memorial Institute 
 * http://www.battelle.org
 * 
 * -------------------------------------------------------------------- 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * -------------------------------------------------------------------- 
 * *
 * TupleIO.java
 *
 *===================================================================*/

public class TupleIO {

	private static final Logger logger = Logger.getLogger(TupleIO.class);
	
	public static TupleList loadCSV(File file, String nameForTuples, 
			TupleListFactory factory) throws IOException {
		try {
			return loadCSV(file, null, ",", 0, 0, nameForTuples, factory, null);
		} catch (CancellationException ce) {
			throw new IOException("loading canceled", ce);
		}
	}

	public static TupleList loadCSV(File file, String nameForTuples, 
			TupleListFactory factory, Cancelable cancelable) throws IOException, CancellationException {
		return loadCSV(file, null, ",", 0, 0, nameForTuples, factory, cancelable);
	}
	
	public static TupleList loadCSV(
			File file, String charSet, 
			String delimiter, 
			int startColumn, int columnCount,
			String nameForTuples,
			TupleListFactory factory) throws IOException {
		try {
			return loadCSV(file, charSet, delimiter, startColumn, columnCount, nameForTuples, factory, null);
		} catch (CancellationException ce) {
			throw new IOException("loading canceled", ce);
		}
	}
		
	public static TupleList loadCSV(
			File file, String charSet, 
			String delimiter, 
			int startColumn, int columnCount,
			String nameForTuples,
			TupleListFactory factory,
			Cancelable cancelable) throws IOException, CancellationException {

		if (charSet == null) {
			charSet = Charset.defaultCharset().name();
		}

		CSVInfo csvInfo = parseCSVInfo(file, charSet, startColumn, delimiter, cancelable);	
		
		BitSet colBits = csvInfo.getColumnBits();

		final int startRow = csvInfo.getStartRow();
		final int rows = csvInfo.getRowCount();
		// Don't have to worry about startColumn.  Bits for columns < startColumn won't be set.
		final int cols = colBits.cardinality();

		if (rows == 0 || cols == 0) {
			throw new IOException(String.format("no data found: rows = %d, columns = %d", rows, cols));
		}

		TupleList tuples = factory.createNewTupleList(nameForTuples, cols, rows);
		double[] buffer = new double[cols];

		BufferedReader br = null;
		boolean ok = false;

		try {

			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
			String line;
			int lineNum = -1;
			int row = 0;

			while((line = br.readLine()) != null) {

				if (cancelable != null && cancelable.isCanceled()) {
					throw new CancellationException();
				}

				line = line.trim();

				if (line.length() > 0) {

					lineNum++;

					if (lineNum >= startRow) {

						try {

							StringTokenizer tokenizer = new StringTokenizer(line, delimiter);
							int tokenCount = tokenizer.countTokens();

							int colIndex = 0;
							for (int i=0; i<tokenCount; i++) {
								String token = tokenizer.nextToken();
								if (colBits.get(i)) {
									buffer[colIndex++] = Double.parseDouble(token);
								}
							}

							tuples.setTuple(row, buffer);

						} catch (NoSuchElementException nsee) {

							throw new IOException("too few columns on row " + row);

						} catch (NumberFormatException nfe) {

							throw new IOException(String.format("unparseable element on row %d: %s",
									lineNum, line));
							
						}

						row++;
					}
				}

			}

			ok = row == rows;

		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
					logger.error(ioe);
				}
			}

			if (!ok) {
				if (tuples != null) {
					try {
						factory.closeTupleList(tuples);
						factory.deleteTupleList(tuples);
					} catch (TupleListFactoryException tlfe) {
						logger.error(tlfe);
					}
				}
			}
		}

		return tuples;
	}
	
//	public static TupleList loadCSV(
//				File file, String charSet, 
//				String delimiter, 
//				int startColumn, int columnCount,
//				String nameForTuples,
//				TupleListFactory factory,
//				Cancelable cancelable) throws IOException, CancellationException {
//	
//		if (charSet == null) {
//			charSet = Charset.defaultCharset().name();
//		}
//
//		int[] fileInfo = countRowsAndColumns(file, charSet, startColumn, delimiter, cancelable);
//		
//		final int rows = fileInfo[0];
//		final int cols = fileInfo[1];
//		final int startRow = fileInfo[2];
//		
//		if (columnCount > 0) {
//			if (cols < startColumn + columnCount) {
//				throw new IOException("too few columns in file: " + cols);
//			}
//		}
//		if (rows == 0 || cols == 0) {
//			throw new IOException(String.format("no data found: rows = %d, columns = %d", rows, cols));
//		}
//
//		final int tupleLength = columnCount > 0 ? columnCount : (cols - startColumn);
//		
//		TupleList tuples = factory.createNewTupleList(nameForTuples, tupleLength, rows);
//		double[] buffer = new double[tupleLength];
//		
//		BufferedReader br = null;
//		boolean ok = false;
//		
//		try {
//						
//			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
//			String line;
//			int lineNum = 0;
//			int row = 0;
//			
//			while((line = br.readLine()) != null) {
//				if (cancelable != null && cancelable.isCanceled()) {
//					throw new CancellationException();
//				}
//				if (lineNum >= startRow) {
//					try {
//						StringTokenizer tokenizer = new StringTokenizer(line, delimiter);
//						for (int i=0; i<startColumn; i++) {
//							tokenizer.nextToken();
//						}
//						for (int i=0; i<tupleLength; i++) {
//							buffer[i] = Double.parseDouble(tokenizer.nextToken());
//						}
//						tuples.setTuple(row, buffer);
//					} catch (NoSuchElementException nsee) {
//						throw new IOException("too few columns on row " + row);
//					} catch (NumberFormatException nfe) {
//						throw new IOException("unparseable element on row " + row + ": " + line);
//					}
//					row++;
//				}
//				lineNum++;
//			}
//			
//			ok = row == rows;
//			
//		} finally {
//		
//			if (br != null) {
//				try {
//					br.close();
//				} catch (IOException ioe) {
//					logger.error(ioe);
//				}
//			}
//			
//			if (!ok) {
//				if (tuples != null) {
//					try {
//						factory.closeTupleList(tuples);
//						factory.deleteTupleList(tuples);
//					} catch (TupleListFactoryException tlfe) {
//						logger.error(tlfe);
//					}
//				}
//			}
//		}
//		
//		return tuples;
//	}
	
	private static int[] countRowsAndColumns(File file, String charSet, 
			int startColumn, String delimiter, Cancelable cancelable) 
					throws IOException, CancellationException {
		
		int rows = 0;
		int cols = 0;
		int startRow = 0;
		
		BufferedReader br = null;
		try {
			
			if (charSet == null) {
				charSet = Charset.defaultCharset().name();
			}
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
			String line;
			int lineNum = 0;
		
			while((line = br.readLine()) != null) {
				
				if (cancelable != null && cancelable.isCanceled()) {
					throw new CancellationException();
				}
				
				if (rows == 0) {
					StringTokenizer tokenizer = new StringTokenizer(line, delimiter);
					int tokenCount = tokenizer.countTokens();
					if (tokenCount > startColumn) {
						// In files with a header row, this will cause the header row to be omitted.
						boolean allOk = true;
						try {
							for (int i=0; i<startColumn; i++) {
								tokenizer.nextToken();
							}
						} catch (NoSuchElementException nsee) {
							allOk = false;
						}
						for (int i=startColumn; i<tokenCount && allOk; i++) {
							try {
								Double.parseDouble(tokenizer.nextToken());
							} catch (NumberFormatException nfe) {
								allOk = false;
							}
						}
						if (allOk) {
							startRow = lineNum;
							rows = 1;
							cols = tokenCount;
						}
					}
				} else {
					rows++;
				}
				
				lineNum++;
			}
			
		} finally {
			
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
				}
			}
			
		}
		
		return new int[] { rows, cols, startRow };
	}
	
	private static CSVInfo parseCSVInfo(File file, String charSet, 
			int startColumn, String delimiter, Cancelable cancelable) 
					throws IOException, CancellationException {
		
		int startRow = -1;
		int lineNum = -1;
		BitSet columnBits = null;
		int expectedTokenCount = -1;
		
		BufferedReader br = null;
		
		try {
			
			if (charSet == null) {
				charSet = Charset.defaultCharset().name();
			}
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
			String line;
		
			while((line = br.readLine()) != null) {
				
				if (cancelable != null && cancelable.isCanceled()) {
					throw new CancellationException();
				}

				line = line.trim();
				
				if (line.length() > 0) {
					
					// The 0-based index of nonblank lines.
					lineNum++;
					
					StringTokenizer tokenizer = new StringTokenizer(line, delimiter);
					int tokenCount = tokenizer.countTokens();
				
					if (startRow < 0) {
					
						if (tokenCount > startColumn) {

							BitSet bits = new BitSet(tokenCount);

							// Throw away everything before the startColumn
							for (int i=0; i<startColumn; i++) {
								tokenizer.nextToken();
							}
						
							for (int i=startColumn; i<tokenCount; i++) {
								try {
									Double.parseDouble(tokenizer.nextToken());
									// Flag the column as containing a parseable double.
									bits.set(i);
								} catch (NumberFormatException nfe) {
									// Don't worry about it.  
								}
							}
						
							if (bits.cardinality() > 0) {
								startRow = lineNum;
								columnBits = bits;
								expectedTokenCount = tokenCount;
							}
						}
						
					} else {
						
						if (tokenCount != expectedTokenCount) {
							throw new IOException(String.format(
									"incorrect number of entries on line %d: %d expected, found %d", 
									(lineNum+1), expectedTokenCount, tokenCount));
						}
						
					}
				
				}
			}
			
		} finally {
			
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
				}
			}
			
		}
		
		if (startRow < 0) {
			throw new IOException("no numeric data found in " + file.getName());
		}
		
		return new CSVInfo(startRow, lineNum - startRow + 1, columnBits);
	}

	public static void saveCSV(File file, TupleList tuples) throws IOException {
		saveCSV(file, null, ",", null, tuples, null);
	}
	
	public static void saveCSV(
			File file, String charSet, String delimiter, String outputPattern, TupleList tuples, String[] headers) throws IOException {
		
		if (charSet == null) {
			charSet = Charset.defaultCharset().name();
		}
		
		final int tupleLength = tuples.getTupleLength();
		final int tupleCount = tuples.getTupleCount();
		
		if (headers != null && headers.length != tupleLength) {
			throw new IOException(String.format("number of headers != tuple length: %d != %d", headers.length, tupleLength));
		}
		
		double[] buffer = new double[tupleLength];
		
		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charSet)));
			if (headers != null) {
				for (int i=0; i<tupleLength; i++) {
					if (i > 0) pw.print(delimiter);
					pw.print(headers[i]);
				}
				pw.println();
			}
			for (int i=0; i<tupleCount; i++) {
				tuples.getTuple(i, buffer);
				for (int j=0; j<tupleLength; j++) {
					if (j > 0) pw.print(delimiter);
					if (outputPattern != null) {
						pw.printf(outputPattern, buffer[j]);
					} else {
						pw.print(buffer[j]);
					}
				}
				pw.println();
			}
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		
	}
	
	static class CSVInfo {
		
		private int startRow;
		private int rowCount;
		private BitSet columnBits;
		
		CSVInfo(int startRow, int rowCount, BitSet columnBits) {
			this.startRow = startRow;
			this.rowCount = rowCount;
			this.columnBits = columnBits;
		}
		
		public int getStartRow() {
			return startRow;
		}
		
		public int getRowCount() {
			return rowCount;
		}
		
		public BitSet getColumnBits() {
			return columnBits;
		}
	}
	
	public static void main(String[] args) {
		
		try {
			
			File f = new File("C:/Users/d3j923/Documents/rguide/battleHistory.csv");
			TupleList tuples = loadCSV(f, null, ",", 4, 0, "battleHistory", new ArrayTupleListFactory());
			
			System.out.printf("tupleLength, tupleCount = (%d, %d)\n", tuples.getTupleLength(), tuples.getTupleCount());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
