package org.silentsquare.dplus.test;

import com.google.apphosting.api.ApiProxy;

public class TestEnvironment implements ApiProxy.Environment  {

	@Override
	public String getAppId() {
		return "Unit Tests";
	}

	@Override
	public String getAuthDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersionId() {
		return "1.0";
	}

	@Override
	public boolean isAdmin() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLoggedIn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDefaultNamespace(String arg0) {
		// TODO Auto-generated method stub
		
	}

}
