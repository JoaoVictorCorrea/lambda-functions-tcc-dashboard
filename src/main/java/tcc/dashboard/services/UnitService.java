package tcc.dashboard.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import tcc.dashboard.models.Unit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UnitService {

    public List<Unit> getActiveUnits(Connection conn, Statement stmt, LambdaLogger logger){

        List<Unit> units = new ArrayList<>();

        try {
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
        }
        catch (Exception e){
            logger.log(" Error in query getActiveUnits: " + e.getMessage());
        }

        return units;
    }
}
