package servlets;

import com.google.gson.JsonObject;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.Main;
import model.UserProfile;
import utils.AccountService;
import utils.AuthHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * alex on 25.09.15.
 */
public class SignInServlet extends HttpServlet {

    public static final String PAGE_URL = "/api/v" + Main.API_VERSION + "/auth/signin";

    private final AccountService accountService;

    public SignInServlet(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

        String code = request.getParameter("code");

        if (code == null) {
            response.sendRedirect("/#login");
        }

        JsonObject jsonObject = new JsonObject();
        String sessionId = request.getSession().getId();

        if (accountService.isLogged(sessionId)) {
            accountService.logout(sessionId);
        }

        UserProfile user = null;

        try {
            user = AuthHelper.getUserFromSocial(code);
        } catch (UnirestException | NullPointerException e) {
            e.printStackTrace();
        }

        if (user != null) {

            if (accountService.isUserExist(user.getEmail())) {
                user = accountService.getUser(user.getEmail());
            } else {
                accountService.signUp(user);
            }

            accountService.signIn(sessionId, user);

            JsonObject userObject = new JsonObject();

            userObject.addProperty("first_name", user.getFirstName());
            userObject.addProperty("last_name", user.getLastName());
            userObject.addProperty("email", user.getEmail());
            userObject.addProperty("avatar", user.getAvatarUrl());
            userObject.addProperty("score", user.getScore());

            jsonObject.addProperty("code", HttpServletResponse.SC_OK);
            jsonObject.add("user", userObject);

            response.setStatus(HttpServletResponse.SC_OK);

        } else {

            jsonObject.addProperty("code", HttpServletResponse.SC_BAD_REQUEST);
            jsonObject.addProperty("description", "Sorry, we could not get your profile from google+");

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().println(jsonObject);
    }
}
