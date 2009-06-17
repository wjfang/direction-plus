package org.silentsquare.dplus.bbctnews;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Route {
	
	private static final Logger logger = Logger.getLogger(Route.class.getName());

	private List<Step> stepList;
	
	private LatLng end;
	
	public final List<Step> getStepList() {
		return stepList;
	}

	public final void setStepList(List<Step> stepList) {
		this.stepList = stepList;
	}

	public final LatLng getEnd() {
		return end;
	}

	public final void setEnd(LatLng end) {
		this.end = end;
	}

	public static Route parseJSON(String rs) throws JSONException {
		logger.fine(rs);
		
		JSONObject route = new JSONObject(rs).getJSONObject("A");
		Route r = new Route();
		
		JSONArray endco = route.getJSONObject("End").getJSONArray("coordinates");
		r.setEnd(new LatLng(endco.getDouble(1), endco.getDouble(0)));
		logger.fine(r.getEnd().toString());
		
		r.setStepList(new ArrayList<Step>());
		JSONArray steps = route.getJSONArray("Steps");
		for (int i = 0; i < steps.length(); i++) {
			JSONObject step = steps.getJSONObject(i);
			Step s = new Step();
			s.setDistance(step.getJSONObject("Distance").getInt("meters"));
			s.setDescriptionHtml(step.getString("descriptionHtml"));
			JSONArray co = step.getJSONObject("Point").getJSONArray("coordinates");
			s.setStart(new LatLng(co.getDouble(1), co.getDouble(0)));
			if (s.isLongEnough()) {
				logger.info(s.guessRoad());
			}
			r.getStepList().add(s);
			logger.fine(s.toString());
		}
		
		return r;
	}

	public static class Step {
		private LatLng start;
		private String descriptionHtml;
		private float distance; /* meters */
		private String road;
		
		public final LatLng getStart() {
			return start;
		}

		public final void setStart(LatLng start) {
			this.start = start;
		}

		public final String getDescriptionHtml() {
			return descriptionHtml;
		}

		public final void setDescriptionHtml(String descriptionHtml) {
			this.descriptionHtml = descriptionHtml;
		}

		public final float getDistance() {
			return distance;
		}

		public final void setDistance(float distance) {
			this.distance = distance;
		}
		
		public boolean isLongEnough() {
			return this.distance > 10000;
		}
		
		public String guessRoad() {
			if (this.road == null) {
				try {
					this.road = _guessRoad();
				} catch (Exception e) {
					logger.warning(e.getLocalizedMessage());
					this.road = "";
				}
			}
			
			return this.road;
		}
		
		private String _guessRoad() {
			for (int i = 0; i < descriptionHtml.length(); ) {
				int j = descriptionHtml.indexOf("<b>", i);
				if (j == -1)
					break;
				i = j + 3; // "<b>".length() == 3
				char c = descriptionHtml.charAt(i); 
				if (c != 'M' && c != 'A') {
					// Assume only A roads and M roads are long enough. 
					continue;
				}
				j = descriptionHtml.indexOf("</b>", i);
				if (j == -1)
					break;
				String candidate = descriptionHtml.substring(i, j);
				if (candidate.indexOf('/') == -1) {
					// Found the first match
					logger.fine(candidate);
					return candidate;
				}
				i = j;
			}
			
			return "";
		}

		@Override
		public String toString() {
			return "start: " + this.start + "\n" 
				+ "descriptionHtml: " + this.descriptionHtml + "\n"
				+ "distance: " + this.distance + " meters"; 			
		}
		
		
	}
	
	public static class LatLng {
		private float latitude;
		private float longitude;
		
		public final float getLatitude() {
			return latitude;
		}
		public final float getLongitude() {
			return longitude;
		}
		
		public LatLng(double lat, double lng) {
			this.latitude = (float) lat;
			this.longitude = (float) lng;
		}
		
		@Override
		public String toString() {
			return "[latitude: " + this.latitude + ", longitude: " + this.longitude + "]";
		}
	}
}
