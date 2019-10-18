package main;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.stream.ActorMaterializer;

import java.io.IOException;
import java.sql.*;

public class Main extends AllDirectives {

    private static final String INSERT = "INSERT INTO bank.accounts (id, balance) values(?, ?)";
    private static final String GET = "SELECT * FROM bank.accounts";


    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost/bank?" +
                "user=testuser&password=password");
    }

    public static void main(String[] args) throws IOException, SQLException {
        var app = new Main();
        Connection connection = app.createConnection();

        PreparedStatement preparedInsert = connection.prepareStatement(INSERT);
        PreparedStatement preparedGet = connection.prepareStatement(GET);

        var actorSystem = ActorSystem.create("ROACH_DB");

        var actorMaterializer = ActorMaterializer.create(actorSystem);
        final var http = Http.get(actorSystem);


        var routeFlow = app.createRoute(preparedInsert, preparedGet).flow(actorSystem, actorMaterializer);
        var binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8090), actorMaterializer);


        System.out.println("Server is running on :8090");
        while(1 < 2) {}
//       binding.thenCompose(ServerBinding::unbind)
//                .thenAccept(unbound -> actorSystem.terminate());


    }

    private Route insert(PreparedStatement ps, long id, long balance) {
        try {
            ps.setLong(1, id);
            ps.setLong(2, balance);
            ps.execute();
            return complete("INSERTED: id=" + id + " balance=" + balance);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return complete("NOT INSERTED");
    }

    private Route getAll(PreparedStatement ps) {
        StringBuilder result = new StringBuilder();
        try {
            final ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                long balance = resultSet.getLong(2);
                result.append("ID: ").append(id).append(" - BALANCE: ").append(balance).append("\n");

            }
            return complete(result.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return complete("OK");
    }

    private Route createRoute(PreparedStatement insert, PreparedStatement get) {
        return concat(
                path("db", () -> concat(
                        get(() -> getAll(get)),
                        post(() -> parameter(StringUnmarshallers.LONG, "id",
                                id -> parameter(StringUnmarshallers.LONG, "balance",
                                        balance -> insert(insert, id, balance))

                        ))


                        )
                )
        );
    }
}
