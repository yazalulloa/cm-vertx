import com.fasterxml.jackson.databind.JsonNode;
import com.yaz.cm.vertx.util.JacksonUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class MySqlTest {


  @Test
  void createSchema(Vertx vertx, VertxTestContext testContext) throws Throwable {

    final var jsonNode = JacksonUtil.yamlMapper().readTree(new File("config/config.yml"));


    final var jsonObject = new JsonObject(jsonNode.toString())
        .getJsonObject("mysql-db")
        .getJsonObject("connect_options");

    final var connectOptions = new MySQLConnectOptions(jsonObject)
        .setProperties(Map.of("ssl", "{\"rejectUnauthorized\":true}"));

    final var poolOptions = new PoolOptions()
        .setShared(true);

    final var pool = MySQLPool.pool(vertx, connectOptions, poolOptions);

    final var query = Files.readString(Paths.get("schema/tables.sql"));

    pool.getConnection()

        .flatMap(sqlConnection -> {
          final var databaseMetadata = sqlConnection.databaseMetadata();

          System.out.println(databaseMetadata.fullVersion());
          System.out.println(databaseMetadata.majorVersion());
          System.out.println(databaseMetadata.minorVersion());
          System.out.println(databaseMetadata.productName());

          if (true) {
            return sqlConnection.close();
          }

          return sqlConnection.query(query)
              .execute()
              .map(rows -> {

                System.out.println(rows);

                rows.forEach(row -> {
                  System.out.println(row.toJson().encodePrettily());
                });
                return rows;
              })
              .flatMap(v -> sqlConnection.close());
        })
        .onComplete(testContext.succeedingThenComplete())
    ;

    testContext.awaitCompletion(60, TimeUnit.SECONDS);

  }


}
