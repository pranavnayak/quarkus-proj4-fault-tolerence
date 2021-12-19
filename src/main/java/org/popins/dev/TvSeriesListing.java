package org.popins.dev;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.popins.dev.model.TvSeries;
import org.popins.dev.proxy.EpisodesProxy;
import org.popins.dev.proxy.TvSeriesProxy;

@Path("/tvseries")
public class TvSeriesListing {
	private List<TvSeries> tvSeries = new ArrayList();
	
	@RestClient
	TvSeriesProxy tvSeriesProxy;
	
	@RestClient
	EpisodesProxy episodesProxy;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Timeout(200)
	@Fallback(fallbackMethod = "fallBackGetTvSeries")
	@CircuitBreaker(
            requestVolumeThreshold = 8,
            failureRatio = 0.5,
            delay = 5000,
            successThreshold = 4)
    @Retry(maxRetries = 5)
	public Response getTvSeries(@QueryParam("title") String title) {
		TvSeries tvs = tvSeriesProxy.getTvSeries(title);
		tvs.setEpisodes(episodesProxy.getEpisodes(tvs.getId()));
		tvSeries.add(tvs);
		return Response.ok(tvSeries).build();
	}
	
	private Response fallBackGetTvSeries(String title) {
		//return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "TEMPORARY GLITCH, PLEASE TRY AFTER SOME TIME.").build();
		return Response.ok("TEMPORARY GLITCH, PLEASE TRY AFTER SOME TIME.").build();
	}
}
