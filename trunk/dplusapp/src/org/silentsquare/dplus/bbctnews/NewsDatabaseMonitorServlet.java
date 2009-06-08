package org.silentsquare.dplus.bbctnews;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silentsquare.dplus.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet implementation class NewsDataBaseQueryServlet
 */
public class NewsDatabaseMonitorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDatabaseMonitorServlet.class.getName());
	
	private NewsDatabase newsDatabase;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDatabaseMonitorServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    	Configuration cfg = (Configuration) ctx.getBean("configuration");
    	newsDatabase = cfg.getNewsDatabase();
    }
    
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, String> status = newsDatabase.monitor();
		
		PrintWriter pw = resp.getWriter();
		pw.println("<html><head>");
		
		pw.println("<title>News Database Status</title>");
		pw.println("<meta http-equiv='pragma' content='no-cache'>");
	    pw.println("<meta http-equiv='cache-control' content='no-cache'>");
	    pw.println("<meta http-equiv='expires' content='0'>");
		pw.println("<style type='text/css'>td {border: thin solid black; padding: 0.5em;}</style>");
		pw.println("</head><body>");
		pw.println("<h3>News Database Status</h3>");
		pw.println("<table>");
		for (String key : status.keySet()) {
			String value = status.get(key);
			pw.println("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
		}
		pw.println("</table></body></html>");
		pw.flush();
	}	
}
