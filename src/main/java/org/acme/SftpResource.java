package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/sftp")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SftpResource {
    @Inject
    SftpService sftpService;

    @POST
    @Path("/transfer")
    public Response transferFile(TransferRequest request) {
        boolean success = sftpService.transferFile(
                request.serverAHost,
                request.userA,
                request.PassA,
                request.remotePathA,
                request.serverBHost,
                request.userB,
                request.passB,
                request.remotePathB
        );

        return success
                ? Response.ok("Transfer complete").build()
                : Response.status(500).entity("Transfer failed").build();
    }
}
