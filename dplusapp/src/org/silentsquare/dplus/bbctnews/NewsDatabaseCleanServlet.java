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
public class NewsDatabaseCleanServlet extends AbstractNewsDatabaseServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(NewsDatabaseCleanServlet.class.getName());
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public NewsDatabaseCleanServlet() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	newsDatabase.clean();		
    }	
}
