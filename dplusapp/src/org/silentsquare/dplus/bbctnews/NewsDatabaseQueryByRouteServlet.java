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

/**
 * Servlet implementation class NewsDataBaseQueryServlet
 */
public class NewsDatabaseQueryByRouteServlet extends AbstractNewsDatabaseServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDatabaseQueryByRouteServlet.class.getName());
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDatabaseQueryByRouteServlet() {
        super();
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
