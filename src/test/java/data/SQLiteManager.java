package data;

import com.google.gson.JsonObject;

import java.sql.*;

public class SQLiteManager {

    public static int counterRowNotAffeted = 0;
    public static int currentAuthorID = 0;
    public static String currentAuthorName = "";

    /**
     * Provides a connection to the SQLite database.
     * This method attempts to establish a connection with an SQLite database using JDBC.
     * If the connection fails, it prints an error message and returns null.
     *
     * @return A {@link Connection} object if successful, or {@code null} if an error occurs.
     */

    public static Connection connect() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:CVLACData.db");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Inserts a researcher into the RESEARCHERS table.
     *
     * @param connection  Active database connection.
     * @param fullName    The full name of the researcher.
     * @param nationality The nationality of the researcher.
     * @return True if insertion is successful, false otherwise.
     */
    public static void insertResearcher(Connection connection, String fullName, String nationality) {
        String sql = "INSERT INTO RESEARCHERS (full_name, nationality) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, nationality);
            if (pstmt.executeUpdate() == 1) {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        currentAuthorID = rs.getInt(1);
                        currentAuthorName = fullName;
                    }
                }
            }
        } catch (SQLException e) {
            String selectSql = "SELECT id FROM RESEARCHERS WHERE full_name = ?";
            String insertSql = "INSERT INTO RESEARCHERS (full_name, nationality) VALUES (?, ?)";

            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                selectStmt.setString(1, fullName);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentAuthorID = rs.getInt("id"); // Researcher already exists

                    }
                }

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static int getCurrentAuthorID() {
        return currentAuthorID;
    }

    public static String getCurrentAuthorName() {
        return currentAuthorName;
    }

    //BORRAS TESTING
    public static void printAllResearchers(Connection connection) {
        String sql = "SELECT * FROM RESEARCHERS;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\nResearchers in the database:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        " | Name: " + rs.getString("full_name") +
                        " | Nationality: " + rs.getString("nationality"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving researchers: " + e.getMessage());
        }
    }


    /**
     * Inserts a bibliographic production into the PRODUCTIONS_BIBLIOGRAPHIC table.
     *
     * @param projectTypeId The associated project type ID.
     */
    public static int insertBibliographicElement(Connection connection, JsonObject bibliographicElement, int projectTypeId, int autorID, String autorFullName) {
        String sql = "INSERT INTO PRODUCTIONS_BIBLIOGRAPHIC (DOI, title, \"ISSN/ISBN\", ED, country, key_words, sectors, areas, tipo_proyecto_id, summary) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        int productionBibliographicID = 0;

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, validateString(bibliographicElement, "DOI"));
            pstmt.setString(2, validateString(bibliographicElement, "title"));
            pstmt.setString(3, (!validateString(bibliographicElement, "ISSN").isEmpty()) ? validateString(bibliographicElement, "ISSN") : validateString(bibliographicElement, "ISBN"));
            pstmt.setString(4, validateString(bibliographicElement, "editorial"));
            pstmt.setString(5, validateString(bibliographicElement, "country"));
            pstmt.setString(6, validateString(bibliographicElement, "key_words"));
            pstmt.setString(7, validateString(bibliographicElement, "sectors"));
            pstmt.setString(8, validateString(bibliographicElement, "areas"));
            pstmt.setInt(9, projectTypeId);
            pstmt.setString(10, validateString(bibliographicElement, "summary"));
            System.out.println("Inserting bibliographic element: " + validateString(bibliographicElement, "title"));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    productionBibliographicID = rs.getInt(1);
                }
            }
            insertRegister(connection, productionBibliographicID, autorFullName, autorID);

        } catch (SQLException e) {
            System.err.println("Error inserting bibliographic data: " + e.getMessage());
        }

        return productionBibliographicID;
    }


    /**
     * Prints all bibliographic productions from the BIBLIOGRAPHIC_DATA table.
     *
     * @param connection The database connection.
     */
    public static void printAllBibliographicElements(Connection connection) {
        String sql = "SELECT * FROM PRODUCTIONS_BIBLIOGRAPHIC;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\nBibliographic elements in the database:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        " | DOI: " + rs.getString("DOI") +
                        " | Title: " + rs.getString("title") +
                        " | ISSN/ISBN: " + rs.getString("ISSN/ISBN") +
                        " | Editorial: " + rs.getString("ED") +
                        " | Country: " + rs.getString("country") +
                        " | Keywords: " + rs.getString("key_words") +
                        " | Sectors: " + rs.getString("sectors") +
                        " | Areas: " + rs.getString("areas") +
                        " | Project Type ID: " + rs.getInt("tipo_proyecto_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving bibliographic elements: " + e.getMessage());
        }
    }

    /**
     * Validates that a given key in a JSON object is not null, not empty, and contains meaningful data.
     *
     * @param json The JSON object containing the data fields.
     * @param key  The key whose value needs validation.
     * @return The validated string if it exists and is not empty; otherwise, returns null.
     **/
    private static String validateString(JsonObject json, String key) {
        return (json.has(key) && json.get(key) != null && !json.get(key).getAsString().trim().isEmpty())
                ? json.get(key).getAsString()
                : "";
    }

    /**
     * Inserts a project type into the TIPO_PROYECTO table.
     *
     * @param connection Active database connection.
     * @param name       The name of the project type.
     * @return True if insertion is successful, false otherwise.
     */
    public static boolean insertProjectType(Connection connection, String name) {
        String sql = "INSERT INTO TIPO_PROYECTO (name) VALUES (?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting project type: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserts a register into the REGISTERS table, linking a researcher with a bibliographic production.
     *
     * @param connection   Active database connection.
     * @param productionId The ID of the bibliographic production.
     * @param author       The author's name.
     * @param researcherId The ID of the researcher.
     * @return True if insertion is successful, false otherwise.
     */
    public static boolean insertRegister(Connection connection, int productionId, String author, int researcherId) {
        String sql = "INSERT INTO REGISTERS (id_productions_bibliographic, author, researcher_id) VALUES (?, ?, ?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productionId);
            pstmt.setString(2, author);
            pstmt.setInt(3, researcherId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting register: " + e.getMessage());
            return false;
        }
    }


    public static void printDatabaseTables(Connection connection) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\tebin\\OneDrive\\Desktop\\Database CvExtractor\\CVLACData.db");
             Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate("INSERT INTO RESEARCHERS (full_name, nationality) VALUES ('Test User', 'Unknown');");
            if (rowsAffected > 0) {
                System.out.println("Test record inserted successfully!");
            } else {
                System.out.println("Insertion failed.");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting test record: " + e.getMessage());
        }


    }


    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:CVLACData.db")) {
            printDatabaseTables(conn);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }

    }


}
