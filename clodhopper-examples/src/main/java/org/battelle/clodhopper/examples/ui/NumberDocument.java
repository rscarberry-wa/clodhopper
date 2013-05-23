package org.battelle.clodhopper.examples.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NumberDocument extends PlainDocument {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7883569618163515985L;
	
	private boolean mAllowSign;
    private boolean mAllowDecimals;
    private boolean mAllowExponent;
    private boolean mAllowNaN;
    private boolean mAllowLeadingZeroes;

    /**
     * Fully-qualified constructor.
     * 
     * @param allowSign if true, the document will accept the plus and negative signs.
     * @param allowDecimals if true, the document will accept periods.
     * @param allowExponent if true, the document will accept the characters &quot;e&quot; and &quot;e&quot;
     * @param allowNaN if true, &quot;NaN&quot; is accepted by the document.
     * @param allowLeadingZeroes if true, allows zeroes to be entered as the leading characters.
     */
    public NumberDocument(boolean allowSign, boolean allowDecimals, 
                  boolean allowExponent, boolean allowNaN,
                  boolean allowLeadingZeroes) {
      mAllowSign = allowSign;
      mAllowDecimals = allowDecimals;
      mAllowExponent = allowExponent;
      mAllowNaN = allowNaN;
      mAllowLeadingZeroes = allowLeadingZeroes;
    }

    public NumberDocument(boolean allowSign, boolean allowDecimals, boolean allowExponent, boolean allowNaN) {
          this(allowSign, allowDecimals, allowExponent, allowNaN, true);
    }
    
    public NumberDocument(boolean allowSign, boolean allowDecimals, boolean allowExponent) {
      this(allowSign, allowDecimals, allowExponent, false);
    }

    public NumberDocument(boolean allowSign, boolean allowDecimals) {
      this(allowSign, allowDecimals, false, false);
    }

    public NumberDocument() {
      this(true, true, false, false);
    }

    public void insertString(int offset, String str, AttributeSet a)
      throws BadLocationException {
      
        int n = str.length();
      
        char prev = ' ';
        if (offset > 0) {
            String s = super.getText(offset - 1, 1);
            prev = s.charAt(0);
        }
        
        for(int i=0; i<n; i++) {
            
            boolean bad = false;
            char c = str.charAt(i);
            
            if (!Character.isDigit(c)) {
                if (c == '+' || c == '-') {
                    if (!(mAllowSign && offset == 0) && !(mAllowExponent && (prev == 'e' || prev == 'E'))) {
                        bad = true;
                    }
                } else if (c == '.') {
                    if (!(mAllowDecimals && (offset == 0 || Character.isDigit(prev)))) {
                        bad = true;
                    }
                } else if (c == 'n' || c == 'N') {
                    if (!(mAllowNaN && ((offset == 0) || (offset == 2 && prev == 'a' || prev == 'A')))) {
                        bad = true;
                    }
                } else if (c == 'a' || c == 'A') {
                    if (!(mAllowNaN && (prev == 'n' || prev == 'N'))) {
                        bad = true;
                    }
                } else if (c == 'e' || c == 'E') {
                    if (!(mAllowExponent && Character.isDigit(prev))) {
                        bad = true;
                    }
                } else {
                    bad = true;
                }
            } else if (c == '0' && i==0 && offset == 0 && !mAllowLeadingZeroes) {
                  bad = true;
            }

            if (bad) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            prev = c;
        }
        
        super.insertString(offset, str, a);
    }
}