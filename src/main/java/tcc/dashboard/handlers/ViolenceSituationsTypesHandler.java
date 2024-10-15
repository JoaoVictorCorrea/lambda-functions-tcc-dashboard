package tcc.dashboard.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import tcc.dashboard.models.Unit;
import tcc.dashboard.models.ViolenceSituationsTypesByUnit;
import tcc.dashboard.services.CryptographyService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViolenceSituationsTypesHandler implements
        RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context){

        var logger = context.getLogger();

        Map<String, String> queryParams = request.getQueryStringParameters();
        String year = queryParams != null ? queryParams.get("year") : null;

        logger.log("Param year - " + year);

        final String DB_IP = System.getenv("DB_IP");
        final String DB_URL = "jdbc:mysql://" + DB_IP +":3306/svsaweb";
        final String DB_USER = System.getenv("DB_USER");
        final String DB_PASS = System.getenv("DB_PASSWORD");
        final String DB_SECRET_KEY = System.getenv("DB_SECRET_KEY");

        List<Unit> units = new ArrayList<>();
        List<ViolenceSituationsTypesByUnit> violenceSituationsTypesByUnits = new ArrayList<>();

        // Loading driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.log("Driver Load.");
        } catch (ClassNotFoundException e) {
            logger.log(" Error during driver load:" + e.getMessage());
        }

        // Connecting with Database
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            logger.log("Database Connected.");

            // Create a Statement to execute queries
            Statement stmt = conn.createStatement();

            // Execute a query
            String query = "SELECT * FROM unidade WHERE tenant_id = 1 AND codigo NOT IN (1, 8, 9, 12, 19, 20)";
            ResultSet rs = stmt.executeQuery(query);

            // Iterate through results
            while (rs.next()) {
                Unit unit = new Unit();
                unit.setId(rs.getLong("codigo"));
                unit.setName(rs.getString("nome"));

                logger.log(unit.getName());

                // Adds each unit in the units list
                units.add(unit);
            }

            // Iterate each unit and consult total of assistance types
            for(Unit unit: units) {

                if(year != null)
                    query = createQueryWithYear(unit, year);
                else
                    query = createQueryWithoutYear(unit);

                // Execute a query
                rs = stmt.executeQuery(query);

                while (rs.next()) {
                    ViolenceSituationsTypesByUnit qtd = new ViolenceSituationsTypesByUnit();

                    qtd.setUnit(unit);
                    qtd.setQtdFisica(rs.getInt("total_qtd_VIOLENCIA_FISICA"));
                    qtd.setQtdPsicologica(rs.getInt("total_qtd_VIOLENCIA_PSICOLOGICA"));
                    qtd.setQtdAbusoOuViolenciaSexual(rs.getInt("total_qtd_ABUSO_OU_E_VIOLENCIA_SEXUAL"));
                    qtd.setQtdAtoInfracional(rs.getInt("total_qtd_ATO_INFRACIONAL"));
                    qtd.setQtdNegligenciaContraCrianca(rs.getInt("total_qtd_NEGLIGENCIA_CONTRA_CRIANÇA"));

                    violenceSituationsTypesByUnits.add(qtd);
                }
            }

            // Close connection with Database
            conn.close();

        } catch (Exception e) {
            logger.log(" Error during connection with database: " + e.getMessage());
        }

        // Convert the unit's list to JSON
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(violenceSituationsTypesByUnits);
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

    private String createQueryWithoutYear(Unit unit){

        return "SELECT"
                + "    SUM(qtd_ABUSO_OU_E_VIOLENCIA_SEXUAL) AS total_qtd_ABUSO_OU_E_VIOLENCIA_SEXUAL, "
                + "    SUM(qtd_VIOLENCIA_FISICA) AS total_qtd_VIOLENCIA_FISICA, "
                + "    SUM(qtd_VIOLENCIA_PSICOLOGICA) AS total_qtd_VIOLENCIA_PSICOLOGICA, "
                + "    SUM(qtd_NEGLIGENCIA_CONTRA_CRIANÇA) AS total_qtd_NEGLIGENCIA_CONTRA_CRIANÇA,"
                + "    SUM(qtd_ATO_INFRACIONAL) AS total_qtd_ATO_INFRACIONAL "
                + "FROM"
                + "    ("
                + "        SELECT"
                + "            SUM(CASE WHEN sv.situacao = 'ABUSO_OU_E_VIOLENCIA_SEXUAL' THEN 1 ELSE 0 END) AS qtd_ABUSO_OU_E_VIOLENCIA_SEXUAL,"
                + "            SUM(CASE WHEN sv.situacao = 'VIOLENCIA_FISICA' THEN 1 ELSE 0 END) AS qtd_VIOLENCIA_FISICA, "
                + "            SUM(CASE WHEN sv.situacao = 'VIOLENCIA_PSICOLOGICA' THEN 1 ELSE 0 END) AS qtd_VIOLENCIA_PSICOLOGICA, "
                + "            SUM(CASE WHEN sv.situacao = 'NEGLIGENCIA_CONTRA_CRIANÇA' THEN 1 ELSE 0 END) AS qtd_NEGLIGENCIA_CONTRA_CRIANÇA, "
                + "            SUM(CASE WHEN sv.situacao = 'ATO_INFRACIONAL' THEN 1 ELSE 0 END) AS qtd_ATO_INFRACIONAL "
                + "        FROM "
                + "            situacaoviolencia sv "
                + "            INNER JOIN pessoa p ON sv.codigo_pessoa = p.codigo "
                + "            INNER JOIN familia f ON p.codigo_familia = f.codigo "
                + "            INNER JOIN prontuario pr ON f.codigo_prontuario = pr.codigo "
                + "            INNER JOIN unidade u ON pr.codigo_unidade = u.codigo "
                + "        WHERE "
                + "            pr.codigo_unidade = " + unit.getId()
                + "            AND sv.tenant_id = 1 "
                + "        ) AS subquery";
    }

    private String createQueryWithYear(Unit unit, String year){

        return "SELECT"
                + "    SUM(qtd_ABUSO_OU_E_VIOLENCIA_SEXUAL) AS total_qtd_ABUSO_OU_E_VIOLENCIA_SEXUAL, "
                + "    SUM(qtd_VIOLENCIA_FISICA) AS total_qtd_VIOLENCIA_FISICA, "
                + "    SUM(qtd_VIOLENCIA_PSICOLOGICA) AS total_qtd_VIOLENCIA_PSICOLOGICA, "
                + "    SUM(qtd_NEGLIGENCIA_CONTRA_CRIANÇA) AS total_qtd_NEGLIGENCIA_CONTRA_CRIANÇA,"
                + "    SUM(qtd_ATO_INFRACIONAL) AS total_qtd_ATO_INFRACIONAL "
                + "FROM"
                + "    ("
                + "        SELECT"
                + "            SUM(CASE WHEN sv.situacao = 'ABUSO_OU_E_VIOLENCIA_SEXUAL' THEN 1 ELSE 0 END) AS qtd_ABUSO_OU_E_VIOLENCIA_SEXUAL,"
                + "            SUM(CASE WHEN sv.situacao = 'VIOLENCIA_FISICA' THEN 1 ELSE 0 END) AS qtd_VIOLENCIA_FISICA, "
                + "            SUM(CASE WHEN sv.situacao = 'VIOLENCIA_PSICOLOGICA' THEN 1 ELSE 0 END) AS qtd_VIOLENCIA_PSICOLOGICA, "
                + "            SUM(CASE WHEN sv.situacao = 'NEGLIGENCIA_CONTRA_CRIANÇA' THEN 1 ELSE 0 END) AS qtd_NEGLIGENCIA_CONTRA_CRIANÇA, "
                + "            SUM(CASE WHEN sv.situacao = 'ATO_INFRACIONAL' THEN 1 ELSE 0 END) AS qtd_ATO_INFRACIONAL "
                + "        FROM "
                + "            situacaoviolencia sv "
                + "            INNER JOIN pessoa p ON sv.codigo_pessoa = p.codigo "
                + "            INNER JOIN familia f ON p.codigo_familia = f.codigo "
                + "            INNER JOIN prontuario pr ON f.codigo_prontuario = pr.codigo "
                + "            INNER JOIN unidade u ON pr.codigo_unidade = u.codigo "
                + "        WHERE "
                + "            pr.codigo_unidade = " + unit.getId()
                + "            AND YEAR(sv.data) = " + year
                + "            AND sv.tenant_id = 1 "
                + "        ) AS subquery";
    }
}
