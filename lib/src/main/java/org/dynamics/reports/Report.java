package org.dynamics.reports;

import com.itextpdf.text.DocumentException;
import org.dynamics.model.Event;

import java.util.List;

public interface Report {
   void generateReport(Event event) throws DocumentException;
   void generateReport(List<Event> event,String title) throws DocumentException;
}
