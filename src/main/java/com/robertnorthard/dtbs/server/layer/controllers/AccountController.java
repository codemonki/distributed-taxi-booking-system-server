package com.robertnorthard.dtbs.server.layer.controllers;

import com.robertnorthard.dtbs.server.exceptions.AccountAlreadyExistsException;
import com.robertnorthard.dtbs.server.exceptions.AccountAuthenticationFailed;
import com.robertnorthard.dtbs.server.exceptions.AccountInvalidException;
import com.robertnorthard.dtbs.server.exceptions.AccountNotFoundException;
import com.robertnorthard.dtbs.server.layer.model.Account;
import com.robertnorthard.dtbs.server.layer.persistence.dto.HttpResponseFactory;
import com.robertnorthard.dtbs.server.layer.service.AccountFacade;
import com.robertnorthard.dtbs.server.layer.service.AccountServiceImpl;
import com.robertnorthard.dtbs.server.layer.utils.datamapper.DataMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * A controller class for receiving and handling all account event related
 * transactions.
 *
 * @author robertnorthard
 */
@Path("/account")
public class AccountController {

    private static final Logger LOGGER = Logger.getLogger(AccountController.class.getName());

    private final AccountFacade accountService;
    private final DataMapper mapper;
    private final HttpResponseFactory responseFactory;

    public AccountController() {
        this.accountService = new AccountServiceImpl();
        this.mapper = DataMapper.getInstance();
        this.responseFactory = HttpResponseFactory.getInstance();
    }

    /**
     * Register new account.
     *
     * @param account JSON representation of an account.
     * @return account object if successful else an appropriate error message: -
     * IOException - invalid JSON - AccountAlreadyExistsException - account with
     * username already exists. - AccountInvalidException - invalid account
     * details.
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response registerAccount(String account) {
        try {
            Account ac = this.mapper.readValue(account, Account.class);
            this.accountService.registerAccount(ac);

            return this.responseFactory.getResponse(
                    ac, Response.Status.OK);
            
        } catch (AccountAlreadyExistsException ex) {
            
            LOGGER.log(Level.WARNING, null, ex);
            return this.responseFactory.getResponse(
                    ex.getMessage(), Response.Status.CONFLICT);
            
        } catch (AccountInvalidException|IOException ex) {

            LOGGER.log(Level.WARNING, null, ex);
            return this.responseFactory.getResponse(
                    ex.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Reset account password.
     *
     * @param username inferred from path parameter.
     * @return account reset confirmation.
     * @throws AccountNotFoundException account with username does not exist.
     */
    @POST
    @Path("/{username}/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response resetAccount(@PathParam("username") String username)
            throws AccountNotFoundException {

        try {
            this.accountService.resetPassword(username);

            return this.responseFactory.getResponse(
                    "Password reset sent", Response.Status.OK);

        } catch (AccountNotFoundException ex) {
            
            LOGGER.log(Level.WARNING, null, ex);
            return this.responseFactory.getResponse(
                    ex.getMessage(), Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Path("/{username}/reset/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response resetPassword(@PathParam("username") String username, @PathParam("code") String code, String message) {

        try {
            JSONObject object = new JSONObject(message);

            this.accountService.resetPassword(code, username, object.getString("password"));

            return this.responseFactory.getResponse(
                    "Password change successful", Response.Status.OK);

        } catch (JSONException ex) {
            
            LOGGER.log(Level.WARNING, null, ex);
            return this.responseFactory.getResponse(
                    ex.getMessage(), Response.Status.BAD_REQUEST);
            
        } catch (AccountAuthenticationFailed ex) {
            
            LOGGER.log(Level.WARNING, null, ex);
            return this.responseFactory.getResponse(
                    ex.getMessage(), Response.Status.UNAUTHORIZED);
            
        } catch (AccountNotFoundException ex) {
            
            LOGGER.log(Level.WARNING, null, ex);
            return this.responseFactory.getResponse(
                    ex.getMessage(), Response.Status.NOT_FOUND);
        }
    }
}
