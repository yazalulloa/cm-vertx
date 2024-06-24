package com.yaz.cm.vertx.verticle;

import com.yaz.cm.vertx.domain.MySqlQueryRequest;
import com.yaz.cm.vertx.util.rx.RetryWithDelay;
import com.yaz.cm.vertx.vertx.BaseVerticle;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLBatchException;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.mysqlclient.MySQLPool;
import io.vertx.rxjava3.sqlclient.SqlClient;
import io.vertx.sqlclient.ClosedConnectionException;
import io.vertx.sqlclient.DatabaseException;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MySqlVerticle extends BaseVerticle {

  public static final String GET_TABLES = address("mysql-get-tables");

  public static final String REQUEST = "mysql-request";
  public static final String TRANSACTION = "mysql-trx";

  private MySQLPool pool;

  @Override
  public void start() throws Exception {

    init();
    eventBusConsumer(REQUEST, this::request);
    eventBusConsumer(TRANSACTION, this::transaction);
    //eventBusConsumerEmptyBody(GET_TABLES, this::getTables);
    vertx.setTimer(TimeUnit.SECONDS.toMillis(3), l -> {
      //subscribe(showMetadata());
    });
  }

  private Completable showMetadata() {
    return pool.getConnection()
        .flatMapCompletable(sqlConnection -> {
          final var databaseMetadata = sqlConnection.databaseMetadata();

          //logger.info("{} {} {} {}", databaseMetadata.fullVersion(), databaseMetadata.majorVersion(), databaseMetadata.minorVersion(), databaseMetadata.productName());

          return sqlConnection.close();
        });

  }


  private Single<List<RowSet<Row>>> transaction(List<MySqlQueryRequest> requests) {

    return pool.withTransaction(sqlConnection -> {
      return Observable.fromIterable(requests)
          .map(request -> executeQuery(sqlConnection, request))
          .map(vertxHandler()::single)
          .toList()
          .toFlowable()
          .flatMap(Single::concat)
          .toList()
          .toMaybe()
          ;
    }).toSingle();
  }


  private Future<RowSet<Row>> executeQuery(SqlClient sqlClient, MySqlQueryRequest request) {
    final var preparedQuery = sqlClient.getDelegate().preparedQuery(request.query());

    if (request.type() == MySqlQueryRequest.Type.NORMAL) {
      if (request.params() != null && !request.params().isEmpty()) {
        final var tuple = request.params().get(0);
        return preparedQuery.execute(tuple);
      }

      return preparedQuery.execute();
    }

    return preparedQuery.executeBatch(request.params());
  }


  private Single<RowSet<Row>> request(MySqlQueryRequest request) {
    return Single.fromCallable(() -> executeQuery(pool, request))
        .flatMap(vertxHandler()::single)
        .retryWhen(RetryWithDelay.retry(10, 100, TimeUnit.MILLISECONDS, throwable -> {

          if (throwable instanceof UnknownHostException) {
            logger.error("RETRYING DB_ERROR", throwable);
            return true;
          }

          if (throwable instanceof DatabaseException) {
            if (throwable instanceof MySQLException mySQLException) {
              final var errorCode = mySQLException.getErrorCode();
              final var sqlState = mySQLException.getSqlState();

              if (errorCode == 1105) {
                logger.info("QUERY {}", request.query());
                return true;
              }

              logger.error("DB_ERROR {} {}", errorCode, sqlState, throwable);
            }

          } else if (throwable instanceof MySQLBatchException mySQLBatchException) {

            final var iterationError = mySQLBatchException.getIterationError();
            logger.error("DB_BATCH_ERROR {}", request, throwable);
            iterationError.forEach((i, t) -> logger.error("DB_BATCH_ERROR_NUMBER {}", i, t));
          } else if (throwable instanceof ClosedConnectionException) {
            return true;
          }
          logger.error("MYSQL_ERROR {}", request.query(), throwable);

          return false;
        }, (retryCount, maxRetryCount) -> {
          // logger.error("DB_ERROR_RETRY {} {}", retryCount, maxRetryCount);
        }));

  }

  private void init() {
    if (pool != null) {
      subscribe(pool.close());
      pool = null;
    }

    final var connectOptions = new MySQLConnectOptions(config().getJsonObject("connect_options"))
        .setProperties(Map.of("ssl", "{\"rejectUnauthorized\":true}"));

    final var poolOptions = new PoolOptions(config().getJsonObject("pool_options"));
    pool = MySQLPool.pool(Vertx.newInstance(vertx), connectOptions, poolOptions);
  }
}
