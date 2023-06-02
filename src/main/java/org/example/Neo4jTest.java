package org.example;
import org.neo4j.driver.*;
import org.stringtemplate.v4.ST;

import static org.neo4j.driver.Values.parameters;


/**
 *   neo4j:
 *     uri: bolt://192.168.1.123:7687
 *     authentication:
 *       username: neo4j
 *       password: happy_cassini
 */



public class Neo4jTest implements AutoCloseable {

    private final Driver driver;

    public Neo4jTest(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }

    public void printGreeting(final String message) {
        try (Session session = driver.session()) {
            String greeting = session.executeWrite(tx -> {
                Query query = new Query("CREATE (a:Greeting) SET a.message = $message RETURN a.message + ', from node ' + id(a)", parameters("message", message));
                Result result = tx.run(query);
                return result.single().get(0).asString();
            });
            System.out.println(greeting);
        }
    }

    public static void main(String... args) {
        try (Neo4jTest greeter = new Neo4jTest("bolt://192.168.1.123:7687", "neo4j", "happy_cassini")) {
            greeter.printGreeting("hello, world");
        }
    }
}