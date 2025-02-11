import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatenbankReader {
    // Pfad zum Verzeichnis, das durchsucht werden soll
    static final String DIRECTORY_PATH = "shared/core/src/main";
    // URL zur Datenbank
    static final String DB_URL = "jdbc:oracle:thin:@xxx";
    // Benutzername für die Datenbankverbindung
    static final String USER = "xxx";
    // Passwort für die Datenbankverbindung
    static final String PASS = "xxx";
    // Liste zum Speichern der Tabellennamen aus der Datenbank
    static List<String> list_table = new ArrayList<>();

    public static void main(String[] args) {
        // Verbindung zur Datenbank herstellen
        verbindungDatenbank();
        // Kopie der Liste der Tabellennamen für die Suche
        List<String> list = list_table;
        // Set für die zu suchenden Tabellennamen erstellen
        Set<String> wordsToFind = new HashSet<>(list);
        // Set für gefundene Tabellennamen erstellen
        Set<String> foundWords = new HashSet<>();

        // Suche nach verwaisten Tabellen beginnen
        System.out.println("Suche nach verwaisten Tabellen beginnt...");
        // Durchsuche Dateien im Verzeichnis nach Tabellennamen
        readFile(DIRECTORY_PATH, wordsToFind, foundWords);
        // Ausgabe der nicht gefundenen Tabellennamen
        System.out.println("Folgende Tabellen wurden im Projekt nicht gefunden:");
        wordsToFind.stream().forEach(System.out::println);
        // Ausgabe der Anzahl der nicht gefundenen Tabellennamen
        System.out.println("Anzahl der nicht gefundenen Tabellen: " + wordsToFind.size());
    }

    // Methode zum Durchsuchen von Dateien nach Tabellennamen
    private static void readFile(String directoryPath, Set<String> wordsToFind, Set<String> foundWords) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    readFile(file.getAbsolutePath(), wordsToFind, foundWords);
                } else {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] words = line.split("\\s+");

                            for (String word : words) {

                                if (!word.isEmpty()) {
                                    String cleanWord = word.toUpperCase();
                                    if (wordsToFind.contains(cleanWord)) {
                                        foundWords.add(cleanWord);
                                    }

                                    for (String found : list_table) {
                                        if (cleanWord.equals("\"" + found + "\")")) {
                                            if (cleanWord.contains(found)) {
                                                foundWords.add(found);
                                            }
                                        }
                                    }
                                }
                            }
                            wordsToFind.removeAll(foundWords);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Methode zur Herstellung der Verbindung zur Datenbank und Abruf der Tabellennamen
    private static void verbindungDatenbank() {
        Connection conn = null;
        try {
            // Verbindung zur Datenbank herstellen
            System.out.println("Verbindung zur Datenbank wird hergestellt...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Metadaten der Datenbank abrufen
            DatabaseMetaData metaData = conn.getMetaData();

            // Tabellennamen abrufen und zur Liste hinzufügen
            ResultSet tables = metaData.getTables(null, metaData.getUserName(), null, new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                // Ignoriere zwischen Tabellen und temporäre Tabellen
                if (!tableName.contains("HT_") && !tableName.contains("LIST")) {
                    list_table.add(tableName);
                }

            }
            tables.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Verbindung schließen
                if (conn != null)
                    conn.close();
                // Ausgabe der Anzahl der gefundenen Tabellennamen
                System.out.println("Anzahl der gefundenen Tabellen: " + list_table.size());
                System.out.println("Verbindung wurde erfolgreich geschlossen.");
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
