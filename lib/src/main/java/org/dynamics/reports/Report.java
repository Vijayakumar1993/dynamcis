package org.dynamics.reports;

import com.itextpdf.text.DocumentException;
import org.dynamics.model.Event;

public interface Report {
   void generateReport( Event event) throws DocumentException;
}
