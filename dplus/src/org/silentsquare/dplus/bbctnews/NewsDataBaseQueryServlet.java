package org.silentsquare.dplus.bbctnews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class NewsDataBaseQueryServlet
 */
public class NewsDataBaseQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDataBaseQueryServlet.class);
	
	private NewsDatabase newsDatabase;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDataBaseQueryServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	newsDatabase = new TestNewsDatabase();
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		 
		String rs = request.getReader().readLine();
		logger.debug(rs);
		
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
			logger.error(e);
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
