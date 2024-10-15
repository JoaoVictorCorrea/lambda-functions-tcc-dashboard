package tcc.dashboard.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import tcc.dashboard.models.AssistanceTypesByUnit;
import tcc.dashboard.models.Unit;
import tcc.dashboard.services.CryptographyService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class AssistanceTypesHandler implements
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
        List<AssistanceTypesByUnit> assistanceTypesByUnits = new ArrayList<>();

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
                    AssistanceTypesByUnit qtd = new AssistanceTypesByUnit();

                    qtd.setUnit(unit);
                    qtd.setQtdAtendimentoRecepcao(rs.getInt("total_qtd_atendimento_recepcao"));
                    qtd.setQtdAtendimentoSocial(rs.getInt("total_qtd_atendimento_social"));
                    qtd.setQtdAtendimentoAtualizacaoCadUnico(rs.getInt("total_qtd_atualizacao_cadunico"));
                    qtd.setQtdAtendimentoCadastramentoCadUnico(rs.getInt("total_qtd_cadastramento_cadunico"));
                    qtd.setQtdVisitaDomiciliar(rs.getInt("total_qtd_visita_domiciliar"));

                    assistanceTypesByUnits.add(qtd);
                }
            }

            // Close connection with Database
            conn.close();

        } catch (Exception e) {
            logger.log(" Error during connection with database: " + e.getMessage());
        }

        // Convert the unit's list to JSON
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(assistanceTypesByUnits);
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

    private String createQueryWithoutYear(Unit unit){

        return "SELECT"
                + "    SUM(qtd_atendimento_recepcao) AS total_qtd_atendimento_recepcao, "
                + "    SUM(qtd_atendimento_social) AS total_qtd_atendimento_social, "
                + "    SUM(qtd_atualizacao_cadunico) AS total_qtd_atualizacao_cadunico, "
                + "    SUM(qtd_cadastramento_cadunico) AS total_qtd_cadastramento_cadunico, "
                + "    SUM(qtd_visita_domiciliar) AS total_qtd_visita_domiciliar "
                + "FROM "
                + "    ( "
                + "        SELECT "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'ATENDIMENTO_RECEPCAO' THEN 1 ELSE 0 END) AS qtd_atendimento_recepcao, "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'ATENDIMENTO_SOCIAL' THEN 1 ELSE 0 END) AS qtd_atendimento_social, "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'ATUALIZACAO_CADUNICO' THEN 1 ELSE 0 END) AS qtd_atualizacao_cadunico, "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'CADASTRAMENTO_CADUNICO' THEN 1 ELSE 0 END) AS qtd_cadastramento_cadunico, "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'VISITA_DOMICILIAR' THEN 1 ELSE 0 END) AS qtd_visita_domiciliar "
                + "        FROM "
                + "            listaatendimento la "
                + "        WHERE "
                + "            la.statusAtendimento = 'ATENDIDO' "
                + "            AND la.codigo_unidade = " + unit.getId()
                + "            AND la.tenant_id = 1 "
                + " "
                + "        UNION ALL "
                + " "
                + "        SELECT "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'ATENDIMENTO_RECEPCAO' THEN 1 ELSE 0 END) AS qtd_atendimento_recepcao, "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'ATENDIMENTO_SOCIAL' THEN 1 ELSE 0 END) AS qtd_atendimento_social, "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'ATUALIZACAO_CADUNICO' THEN 1 ELSE 0 END) AS qtd_atualizacao_cadunico, "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'CADASTRAMENTO_CADUNICO' THEN 1 ELSE 0 END) AS qtd_cadastramento_cadunico, "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'VISITA_DOMICILIAR' THEN 1 ELSE 0 END) AS qtd_visita_domiciliar "
                + "        FROM "
                + "            agendamentofamiliar af "
                + "        WHERE  "
                + "            af.statusAtendimento = 'ATENDIDO' "
                + "            AND af.codigo_unidade = " + unit.getId()
                + "            AND af.tenant_id = 1 "
                + "    ) AS combined_results";
    }

    private String createQueryWithYear(Unit unit, String year){

        return "SELECT"
                + "    SUM(qtd_atendimento_recepcao) AS total_qtd_atendimento_recepcao, "
                + "    SUM(qtd_atendimento_social) AS total_qtd_atendimento_social, "
                + "    SUM(qtd_atualizacao_cadunico) AS total_qtd_atualizacao_cadunico, "
                + "    SUM(qtd_cadastramento_cadunico) AS total_qtd_cadastramento_cadunico, "
                + "    SUM(qtd_visita_domiciliar) AS total_qtd_visita_domiciliar "
                + "FROM "
                + "    ( "
                + "        SELECT "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'ATENDIMENTO_RECEPCAO' THEN 1 ELSE 0 END) AS qtd_atendimento_recepcao, "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'ATENDIMENTO_SOCIAL' THEN 1 ELSE 0 END) AS qtd_atendimento_social, "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'ATUALIZACAO_CADUNICO' THEN 1 ELSE 0 END) AS qtd_atualizacao_cadunico, "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'CADASTRAMENTO_CADUNICO' THEN 1 ELSE 0 END) AS qtd_cadastramento_cadunico, "
                + "            SUM(CASE WHEN la.codigoAuxiliar = 'VISITA_DOMICILIAR' THEN 1 ELSE 0 END) AS qtd_visita_domiciliar "
                + "        FROM "
                + "            listaatendimento la "
                + "        WHERE "
                + "            la.statusAtendimento = 'ATENDIDO' "
                + "            AND la.codigo_unidade = " + unit.getId()
                + "            AND YEAR(la.dataAtendimento) = " + year
                + "            AND la.tenant_id = 1 "
                + " "
                + "        UNION ALL "
                + " "
                + "        SELECT "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'ATENDIMENTO_RECEPCAO' THEN 1 ELSE 0 END) AS qtd_atendimento_recepcao, "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'ATENDIMENTO_SOCIAL' THEN 1 ELSE 0 END) AS qtd_atendimento_social, "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'ATUALIZACAO_CADUNICO' THEN 1 ELSE 0 END) AS qtd_atualizacao_cadunico, "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'CADASTRAMENTO_CADUNICO' THEN 1 ELSE 0 END) AS qtd_cadastramento_cadunico, "
                + "            SUM(CASE WHEN af.codigoAuxiliar = 'VISITA_DOMICILIAR' THEN 1 ELSE 0 END) AS qtd_visita_domiciliar "
                + "        FROM "
                + "            agendamentofamiliar af "
                + "        WHERE  "
                + "            af.statusAtendimento = 'ATENDIDO' "
                + "            AND af.codigo_unidade = " + unit.getId()
                + "            AND YEAR(af.dataAtendimento) = " + year
                + "            AND af.tenant_id = 1 "
                + "    ) AS combined_results";
    }
}
