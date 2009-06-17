package org.silentsquare.dplus.bbctnews;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.silentsquare.dplus.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet implementation class NewsDataBaseQueryServlet
 */
public class NewsDatabaseQueryByRouteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDatabaseQueryByRouteServlet.class.getName());
	
	private NewsDatabase newsDatabase;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDatabaseQueryByRouteServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    	Configuration cfg = (Configuration) ctx.getBean("configuration");
    	newsDatabase = cfg.getNewsDatabase();
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		 
		String rs = request.getReader().readLine();
		logger.fine(rs);
		
		Route route = null;
		try {
			route = Route.parseJSON(rs);
		} catch (JSONException e) {
			logger.severe(e.getMessage());
			throw new ServletException(e);
		}		
		
		List<News> results = newsDatabase.query(route);
		JSONArray ja = new JSONArray();
		for (News news : results) {
			ja.put(new JSONObject(news));
		}
		response.getWriter().println(ja);
	}	
}
