package org.silentsquare.dplus.bbctnews;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.silentsquare.dplus.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet implementation class NewsDataBaseQueryServlet
 */
public abstract class AbstractNewsDatabaseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected NewsDatabase newsDatabase;
	
	protected String updateKey;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public AbstractNewsDatabaseServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    	Configuration cfg = (Configuration) ctx.getBean("configuration");
    	newsDatabase = cfg.getNewsDatabase();
    	updateKey = cfg.getUpdateKey();
    }
    	
}
