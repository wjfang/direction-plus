package org.silentsquare.dplus.bbctnews;

import java.io.IOException;
import java.util.ArrayList;
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
public class NewsDatabaseQueryServlet extends AbstractNewsDatabaseServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDatabaseQueryServlet.class.getName());
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDatabaseQueryServlet() {
        super();
    }

    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		 
		String rs = request.getReader().readLine();
		logger.fine(rs);
		
		List<float[]> waypoints = new ArrayList<float[]>();
		try {
			JSONArray pa = new JSONArray(rs);
			for (int i = 0; i < pa.length(); i++) {
				float[] point = new float[2];
				JSONArray po = pa.getJSONArray(i);
				point[0] = (float) po.getDouble(0);
				point[1] = (float) po.getDouble(1);
				waypoints.add(point);
			}
		} catch (JSONException e) {
			logger.severe(e.getMessage());
			throw new ServletException(e);
		}		
		
		List<News> results = newsDatabase.query(waypoints);
		JSONArray ja = new JSONArray();
		for (News news : results) {
			ja.put(new JSONObject(news));
		}
		response.getWriter().println(ja);
	}	
}
