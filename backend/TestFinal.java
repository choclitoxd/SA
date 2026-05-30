import java.sql.*;
public class TestFinal {
    public static void main(String[] args) throws Exception {
        String host = "mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com";
        String pass = System.getProperty("db.pass");
        String[] urls = {
            "jdbc:mariadb://" + host + ":15029/defaultdb?sslMode=DISABLE&allowPublicKeyRetrieval=true",
            "jdbc:mariadb://" + host + ":15029/defaultdb?sslMode=DISABLE&useServerPrepStmts=true&allowPublicKeyRetrieval=true",
            "jdbc:mysql://" + host + ":15029/defaultdb?sslMode=DISABLED&allowPublicKeyRetrieval=true&useSSL=false",
        };
        String MJAR = "C:/Users/leoga/.m2/repository/org/mariadb/jdbc/mariadb-java-client/3.4.2/mariadb-java-client-3.4.2.jar";
        String MYJAR = "C:/Users/leoga/.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar";
        
        for (String url : urls) {
            System.out.println("\n--- " + url.substring(url.indexOf("?")+1));
            long t0 = System.currentTimeMillis();
            try {
                Connection c = DriverManager.getConnection(url, "avnadmin", pass);
                System.out.println("CONECTADO en " + (System.currentTimeMillis()-t0) + "ms");
                ResultSet r = c.createStatement().executeQuery("SELECT VERSION(), @@ssl_cipher");
                r.next();
                System.out.println("MySQL: " + r.getString(1) + " | SSL cipher: " + r.getString(2));
                c.close(); return;
            } catch(Exception e) {
                System.out.println("FALLO en " + (System.currentTimeMillis()-t0) + "ms: " + e.getMessage());
            }
        }
    }
}
