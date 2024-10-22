package tcc.dashboard.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import tcc.dashboard.models.Unit;
import tcc.dashboard.services.CryptographyService;
import tcc.dashboard.services.UnitService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitsHandler implements
        RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context){

        var logger = context.getLogger();

        String query = "";

        final String DB_IP = System.getenv("DB_IP");
        final String DB_URL = "jdbc:mysql://" + DB_IP +":3306/svsaweb";
        final String DB_USER = System.getenv("DB_USER");
        final String DB_PASS = System.getenv("DB_PASSWORD");
        final String DB_SECRET_KEY = System.getenv("DB_SECRET_KEY");

        List<Unit> units = new ArrayList<>();

        // Loading driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.log(" Driver Load.");
        } catch (ClassNotFoundException e) {
            logger.log(" Error during driver load:" + e.getMessage());
        }

        // Connecting with Database
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            logger.log(" Database Connected.");

            // Create a Statement to execute queries
            Statement stmt = conn.createStatement();

            // Get the units
            UnitService unitService = new UnitService();
            units = unitService.getActiveUnits(conn, stmt, logger);

            // Close connection with Database
            conn.close();

        } catch (Exception e) {
            logger.log(" Error during connection with database: " + e.getMessage());
        }

        // Convert the unit's list to JSON
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(units);
        logger.log(" Generated Json");

        String encryptedJson = "";

        // Encrypt JsonResponse
        try {
            CryptographyService cryptographyService = new CryptographyService();
            encryptedJson = cryptographyService.encryptWithAES(jsonResponse, DB_SECRET_KEY);
            logger.log(" Encrypted Json");
        }
        catch (Exception e){
            logger.log(" Error in encrypt Json: " + e.getMessage());
        }

        // Create response HTTP
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(encryptedJson);
        response.setIsBase64Encoded(false);

        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Content-Type", "application/json");

        response.setHeaders(headers);

        return response;
    }
}
