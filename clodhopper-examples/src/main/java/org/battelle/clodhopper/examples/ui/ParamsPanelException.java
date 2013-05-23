package org.battelle.clodhopper.examples.ui;

import java.util.ArrayList;
import java.util.List;

public class ParamsPanelException extends Exception {

	private static final long serialVersionUID = 5153880250362481099L;
	private List<String> errorList;
	
	public ParamsPanelException(String message, List<String> errorList) {
		super(message);
		if (errorList != null) {
			this.errorList = errorList;
		} else {
			this.errorList = new ArrayList<String> ();
		}
	}
	
	public List<String> getErrorList() {
		return errorList;
	}
}
