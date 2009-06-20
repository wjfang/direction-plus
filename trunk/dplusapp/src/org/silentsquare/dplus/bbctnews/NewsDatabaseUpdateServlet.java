package org.silentsquare.dplus.bbctnews;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class NewsDataBaseQueryServlet
 */
public class NewsDatabaseUpdateServlet extends AbstractNewsDatabaseServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDatabaseUpdateServlet.class.getName());
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDatabaseUpdateServlet() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String key = req.getParameter("key");
		if (!updateKey.equals(key)) {
			logger.severe("Incorrect update key.");
			return;
		}
		
		newsDatabase.update();
	}	
}
