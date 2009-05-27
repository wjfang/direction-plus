package org.silentsquare.dplus.bbctnews;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.silentsquare.dplus.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet implementation class NewsDataBaseQueryServlet
 */
public class NewsDatabaseUpdateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDatabaseUpdateServlet.class);
	
	private NewsDatabase newsDatabase;
	
	private String updateKey;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDatabaseUpdateServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    	Configuration cfg = (Configuration) ctx.getBean("configuration");
    	newsDatabase = cfg.getNewsDatabase();
    	updateKey = cfg.getUpdateKey();
    }
    
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String key = req.getParameter("key");
		if (!updateKey.equals(key)) {
			logger.error("Incorrect update key.");
			return;
		}
		
		newsDatabase.update();
	}	
}
