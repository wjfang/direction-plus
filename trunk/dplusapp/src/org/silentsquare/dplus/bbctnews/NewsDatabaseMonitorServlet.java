package org.silentsquare.dplus.bbctnews;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silentsquare.dplus.bbctnews.NewsDatabase.StatusEntry;
import org.silentsquare.dplus.bbctnews.NewsDatabase.StatusPart;

/**
 * Servlet implementation class NewsDataBaseQueryServlet
 */
public class NewsDatabaseMonitorServlet extends AbstractNewsDatabaseServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDatabaseMonitorServlet.class.getName());
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDatabaseMonitorServlet() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<StatusPart> status = newsDatabase.monitor();
		
		PrintWriter pw = resp.getWriter();
		pw.println("<html><head>");
		pw.println("<title>News Database Status</title>");
		pw.println("<meta http-equiv='pragma' content='no-cache'>");
	    pw.println("<meta http-equiv='cache-control' content='no-cache'>");
	    pw.println("<meta http-equiv='expires' content='0'>");
		pw.println("<style type='text/css'>td {border: thin solid black; padding: 0.5em;}</style>");
		pw.println("</head><body>");
		pw.println("<h2>News Database Status</h2>");
		
		for (StatusPart part : status) {
			outputTable(pw, part.title, part.content);
		}
		
		pw.println("</body></html>");
		pw.flush();
	}

	private void outputTable(PrintWriter pw, String head, List<StatusEntry> list) {
		pw.println("<h3>" + head + "</h3>");
		pw.println("<table>");
		for (StatusEntry entry : list) {
			pw.println("<tr><td>" + entry.key + "</td><td>" + entry.value + "</td></tr>");
		}
		pw.println("</table>");		
	}	
}
