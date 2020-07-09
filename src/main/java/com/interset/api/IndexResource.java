package com.interset.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/index")
public class IndexResource {

    private static final Logger LOG = LoggerFactory.getLogger(QueryResource.class);

    /*
        Used for bonus.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response index(

    ) {
        throw new UnsupportedOperationException("IndexResource.index method is not implemented yet.");
    }


}
