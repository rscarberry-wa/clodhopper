package org.battelle.clodhopper.examples.ui;

import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

import org.battelle.clodhopper.distance.CanberraDistanceMetric;
import org.battelle.clodhopper.distance.ChebyshevDistanceMetric;
import org.battelle.clodhopper.distance.CosineDistanceMetric;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.distance.ManhattanDistanceMetric;
import org.battelle.clodhopper.distance.TanimotoDistanceMetric;

/**
 * Contains static utility methods to be used by user interfaces.
 * 
 * @author D3J923
 *
 */
public final class UIUtils {

	private static final Map<String, DistanceMetric> DISTANCE_METRICS;
    
    static {
        DISTANCE_METRICS = new LinkedHashMap<String, DistanceMetric>();
        DISTANCE_METRICS.put("Canberra", new CanberraDistanceMetric());
        DISTANCE_METRICS.put("Chebyshev", new ChebyshevDistanceMetric());
        DISTANCE_METRICS.put("Cosine", new CosineDistanceMetric());
        DISTANCE_METRICS.put("Euclidean", new EuclideanDistanceMetric());
        DISTANCE_METRICS.put("Manhattan", new ManhattanDistanceMetric());
        DISTANCE_METRICS.put("Tanimoto", new TanimotoDistanceMetric());
    }

    private UIUtils() { /* Uninstantiable */ }
	
    public static Set<String> distanceMetricNames() {
    	return DISTANCE_METRICS.keySet();
    }
    
    public static DistanceMetric distanceMetric(String name) {
    	return DISTANCE_METRICS.get(name);
    }
    
    public static String properEnumName(String enumName) {
    	final int len = enumName.length();
    	StringBuilder sb = new StringBuilder(len);
    	if (len > 0) {
    		sb.append(Character.toUpperCase(enumName.charAt(0)));
    		for (int i=1; i<len; i++) {
    			sb.append(Character.toLowerCase(enumName.charAt(i)));
    		}
    	}
    	return sb.toString();
    }
    
    /**
	 * Parses an integer value from the text present in a text component.  It also ensures the value
	 * falls between the specified minimum and maximum, inclusive of both.  If not, or if the text
	 * is empty or cannot be parsed into an integer, this method throws an IllegalArgumentException
	 * containing an appropriate message.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name a name for the value to be parsed.  This is used in constructing an information error message.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
	 * @param minInclusive whether the minimum is inclusive.
	 * @param maxInclusive whether the maximum is inclusive.
	 * @return the entered value.
	 * @throws IllegalArgumentException if the conditions are not met.
	 */
	public static int extractInt(JTextComponent textComponent, String name, int minimum, int maximum, boolean minInclusive, boolean maxInclusive) 
		throws IllegalArgumentException {
		
		try {
			String text = textComponent.getText().trim();
			if (text.length() > 0) {
				int value = Integer.parseInt(text);
				if (checkGreater(value, minimum, minInclusive) && checkLess(value, maximum, maxInclusive)) {
					return value;
				}
			}
		} catch (NumberFormatException nfe) {
		}
		
		String message = null;
		if (maximum == Integer.MAX_VALUE) {
			if (minimum != Integer.MIN_VALUE) {
				if (minInclusive) {
					message = String.format("A value of at least %d must be entered for %s", minimum, name);
				} else {
					message = String.format("A value greater than %d must be entered for %s", minimum, name);
				}
			} else {
				message = "A valid integer must be entered for " + name;
			}
		} else {
			if (minimum != Integer.MIN_VALUE) {
				message = String.format("A value in the range %s%d - %d%s must be entered for %s", (minInclusive ? "[" : "("), 
						minimum, maximum, (maxInclusive ? "]" : ")"), name);
			} else {
				message = String.format("A value less than %s%d must be entered for %s", (maxInclusive ? "or equal to " : ""), maximum, name);
			}
		}
		
		throw new IllegalArgumentException(message);
	}
	
	private static boolean checkGreater(int value, int min, boolean inclusive) {
		return value > min || (inclusive && value == min);
	}
	
	private static boolean checkLess(int value, int max, boolean inclusive) {
		return value < max || (inclusive && value == max);
	}

	private static boolean checkGreater(long value, long min, boolean inclusive) {
		return value > min || (inclusive && value == min);
	}
	
	private static boolean checkLess(long value, long max, boolean inclusive) {
		return value < max || (inclusive && value == max);
	}

	private static boolean checkGreater(double value, double min, boolean inclusive) {
		return value > min || (inclusive && value == min);
	}
	
	private static boolean checkLess(double value, double max, boolean inclusive) {
		return value < max || (inclusive && value == max);
	}
	/**
	 * Similar to variant of <code>extractInt()</code> which takes all the same parameters except for the
	 * list <code>errorMessages</code>.  This method traps the IllegalArgumentException if thrown, and adds
	 * the error message to the list.  Callers should check the size of the list after the call to determine
	 * whether the value was parsed successfully.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name the name for the value being extracted.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
	 * @param errorMessages list into which to copy error messages if conditions are not met.
	 * @return the entered value.
	 */
	public static int extractInt(JTextComponent textComponent, String name, int minimum, int maximum, List<String> errorMessages) {
		try {
			return extractInt(textComponent, name, minimum, maximum, true, true);
		} catch (IllegalArgumentException e) {
			errorMessages.add(e.getMessage());
		}
		return 0;
	}
	
	/**
	 * Parses an integer value from the text present in a text component if the text is not blank.  
	 * It also ensures the value falls between the specified minimum and maximum, inclusive of both.  
	 * If not, or if the text cannot be parsed into an integer, this method throws an IllegalArgumentException
	 * containing an appropriate message.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name a name for the value to be parsed.  This is used in constructing an information error message.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
	 * @param defaultIfBlank the value to return if the text in the component is empty or all whitespace.
	 * @return the entered value.
	 * @throws IllegalArgumentException if boundary conditions are not met.
	 */
	public static int extractInt(JTextComponent textComponent, String name, int minimum, int maximum, int defaultIfBlank)
		throws IllegalArgumentException {

		String text = textComponent.getText().trim();
		if (text.length() > 0) {
			return extractInt(textComponent, name, minimum, maximum, true, true);
		}
		
		return defaultIfBlank;
	}

	/**
	 * Parses an long value from the text present in a text component.  It also ensures the value
	 * falls between the specified minimum and maximum, inclusive of both.  If not, or if the text
	 * is empty or cannot be parsed into an integer, this method throws an IllegalArgumentException
	 * containing an appropriate message.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name a name for the value to be parsed.  This is used in constructing an information error message.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
	 * @return the entered value.
	 * @throws IllegalArgumentException if boundary conditions are not met.
	 */
	public static long extractLong(JTextComponent textComponent, String name, long minimum, long maximum) 
		throws IllegalArgumentException {
		
		try {
			String text = textComponent.getText().trim();
			if (text.length() > 0) {
				long value = Long.parseLong(text);
				if (value >= minimum && value <= maximum) {
					return value;
				}
			}
		} catch (NumberFormatException nfe) {
		}
		
		String message = null;
		if (maximum == Long.MAX_VALUE) {
			if (minimum != Long.MIN_VALUE) {
				message = String.format("A value of at least %d must be entered for %s", minimum, name);
			} else {
				message = "A valid value must be entered for " + name;
			}
		} else {
			if (minimum != Long.MIN_VALUE) {
				message = String.format("A value in the range [%d - %d] must be entered for %s", minimum, maximum, name);
			} else {
				message = String.format("A value less than or equal to %d must be entered for %s", maximum, name);
			}
		}
		
		throw new IllegalArgumentException(message);
	}
	
	/**
	 * Parses an long value from the text present in a text component.  It also ensures the value
	 * falls between the specified minimum and maximum, inclusive of both.  If not, or if the text
	 * is empty or cannot be parsed into an integer, this method throws an IllegalArgumentException
	 * containing an appropriate message.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name a name for the value to be parsed.  This is used in constructing an information error message.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
         * @param errorMessages a list for collecting error messages if conditions are not met.
	 * @return the entered value.
	 */
	public static long extractLong(JTextComponent textComponent, String name, long minimum, long maximum, 
			List<String> errorMessages) {
		try {
			return extractLong(textComponent, name, minimum, maximum);
		} catch (IllegalArgumentException e) {
			errorMessages.add(e.getMessage());
		}
		return 0L;
	}

	/**
	 * Parses a long value from the text present in a text component if the text is not blank.  
	 * It also ensures the value falls between the specified minimum and maximum, inclusive of both.  
	 * If not, or if the text cannot be parsed into a long, this method throws an IllegalArgumentException
	 * containing an appropriate message.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name a name for the value to be parsed.  This is used in constructing an information error message.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
	 * @param defaultIfBlank the value to return if the text in the component is empty or all whitespace.
	 * @return the entered value.
	 * @throws IllegalArgumentException if boundary conditions are not met.
	 */
	public static long extractLong(JTextComponent textComponent, String name, 
			long minimum, long maximum, long defaultIfBlank)
		throws IllegalArgumentException {

		String text = textComponent.getText().trim();
		if (text.length() > 0) {
			return extractLong(textComponent, name, minimum, maximum);
		}
		
		return defaultIfBlank;
	}
	
	/**
	 * Parses a long value from the text present in a text component if the text is not blank.  
	 * It also ensures the value falls between the specified minimum and maximum, inclusive of both.
         * If the field is blank, a default value is used instead. If exceptions are thrown, their messages are
         * collected in a list.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name a name for the value to be parsed.  This is used in constructing an information error message.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
	 * @param defaultIfBlank the value to return if the text in the component is empty or all whitespace.
         * @param errorMessages list for collecting validation error messages.
	 * @return the entered value.
	 */
	public static long extractLong(JTextComponent textComponent, String name, 
			long minimum, long maximum, long defaultIfBlank, List<String> errorMessages)
		throws IllegalArgumentException {

		try {
			return extractLong(textComponent, name, minimum, maximum, defaultIfBlank);
		} catch (IllegalArgumentException e) {
			errorMessages.add(e.getMessage());
		}
		
		return 0L;
	}

	/**
	 * Parses an double value from the text present in a text component.  It also ensures the value
	 * falls between the specified minimum and maximum, inclusive of both.  If not, or if the text
	 * is empty or cannot be parsed into a double, this method throws an IllegalArgumentException
	 * containing an appropriate message.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name a name for the value to be parsed.  This is used in constructing an information error message.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
         * @param minInclusive whether of not the minimum is inclusive.
         * @param maxInclusive whether or not the maximum is inclusive.
	 * @return the entered value.
	 * @throws IllegalArgumentException if boundary conditions are not met.
	 */
	public static double extractDouble(JTextComponent textComponent, String name, 
			double minimum, double maximum, boolean minInclusive, boolean maxInclusive) 
		throws IllegalArgumentException {
		
		try {
			String text = textComponent.getText().trim();
			if (text.length() > 0) {
				double value = Double.parseDouble(text);
				if ((Double.isNaN(minimum) || checkGreater(value, minimum, minInclusive)) && 
						(Double.isNaN(maximum) || checkLess(value, maximum, maxInclusive))) {
					return value;
				}
			}
		} catch (NumberFormatException nfe) {
		}
		
		String message = null;
		if (maximum == Double.MAX_VALUE || Double.isNaN(maximum)) {
			if (minimum != -Double.MAX_VALUE && !Double.isNaN(minimum)) {
				message = String.format("A value %s %d must be entered for %s", (minInclusive ? "of at least" : "greater than"), minimum, name);
			} else {
				message = "A valid value must be entered for " + name;
			}
		} else {
			if (minimum != -Double.MAX_VALUE && !Double.isNaN(minimum)) {
				message = String.format("A value in the range %s%d - %d%s must be entered for %s", 
						(minInclusive ? "[" : "("), minimum, maximum, (maxInclusive ? "]" : ")"), name);
			} else {
				message = String.format("A value less than %s%d must be entered for %s", (maxInclusive ? "or equal to " : ""), maximum, name);
			}
		}
		
		throw new IllegalArgumentException(message);
	}

	/**
	 * Similar to variant of <code>extractDouble()</code> which takes all the same parameters except for the
	 * list <code>errorMessages</code>.  This method traps the IllegalArgumentException if thrown, and adds
	 * the error message to the list.  Callers should check the size of the list after the call to determine
	 * whether the value was parsed successfully.
	 * 
	 * @param textComponent the text component from which to extract the value.
	 * @param name a name for the value to be parsed.  This is used in constructing an information error message.
	 * @param minimum the minimum value allowed.
	 * @param maximum the maximum value allowed.
         * @param minInclusive whether the minimum value is inclusive.
         * @param maxInclusive whether the maximum value is inclusive.
         * @param errorMessages list for collecting validation error messages.
	 * @return the entered value.
	 */
	public static double extractDouble(JTextComponent textComponent, String name, 
			double minimum, double maximum, boolean minInclusive, boolean maxInclusive, List<String> errorMessages) {
		try {
			return extractDouble(textComponent, name, minimum, maximum, minInclusive, maxInclusive);
		} catch (IllegalArgumentException e) {
			errorMessages.add(e.getMessage());
		}
		return 0;
	}

        /**
         * Displays an error dialog.
         * @param comp parent component of the dialog.
         * @param title the title.
         * @param errorMessages messages displayed in the dialog.
         */
	public static void displayErrorDialog(Component comp, String title, List<String> errorMessages) {
		JOptionPane.showMessageDialog(comp, 
				constructMultilineHTMLMessage(errorMessages), 
				title, JOptionPane.ERROR_MESSAGE);
	}
	
        /**
         * Constructs a multiple line HTML message from a list of messages.
         * @param messages the list of messages.
         * @return an HTML message.
         */
	public static String constructMultilineHTMLMessage(List<String> messages) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		final int n = messages.size();
		if (n > 0) {
			sb.append(messages.get(0));
			for (int i=1; i<n; i++) {
				sb.append("<br>");
				sb.append(messages.get(i));
			}
		}
		sb.append("</html>");
		return sb.toString();
	}
}
