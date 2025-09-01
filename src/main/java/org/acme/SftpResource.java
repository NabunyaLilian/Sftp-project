package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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

    @POST
    @Path("/upload")
    public Response UploadFile() {
        boolean success = sftpService.uploadFile();

        return success
                ? Response.ok("Transfer complete").build()
                : Response.status(500).entity("Transfer failed").build();
    }

    @GET
    @Path("/download-files")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadFiles() {
        boolean success = sftpService.downloadAllZips();

        if (success) {
            return Response.ok("{\"status\":\"Download complete\"}").build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"Download failed\"}")
                    .build();
        }
    }
}
