package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = Main.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static Lesson getScheduleForWebinar( String spreadsheetId, String subjectName, String range) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .setMajorDimension("COLUMNS")
                .execute();

        Spreadsheet response1 = service.spreadsheets()
                .get(spreadsheetId)
                .setRanges(Collections.singletonList(range))
                .setIncludeGridData(true)
                .execute();

        List<CellIndex> matchingColorCells = new ArrayList<>();
        Color targetColor = new Color().setRed(0.8509804f).setGreen(0.91764706f).setBlue(0.827451f);

        if (response1 != null && !response1.getSheets().isEmpty()) {
            Sheet sheet = response1.getSheets().get(0);
            if (sheet.getData() != null && !sheet.getData().isEmpty()) {
                List<RowData> rowDataList = sheet.getData().get(0).getRowData();

                for (int rowIndex = 0; rowIndex < rowDataList.size(); rowIndex++) {
                    RowData rowData = rowDataList.get(rowIndex);
                    for (int columnIndex = 0; columnIndex < rowData.getValues().size(); columnIndex++) {
                        CellData cell = rowData.getValues().get(columnIndex);
                        if (cell.getUserEnteredFormat() != null && cell.getUserEnteredFormat().getBackgroundColor() != null) {
                            Color cellColor = cell.getUserEnteredFormat().getBackgroundColor();
                            if (colorsMatch(cellColor, targetColor)) {
                                matchingColorCells.add(new CellIndex(columnIndex, rowIndex));
                            }
                        }
                    }
                }
            }
        }
        List<Integer> list = new ArrayList<>();
        List<Integer> list1 = new ArrayList<>();
        Lesson TP = new Lesson(subjectName, list, list1);
        for (CellIndex cellIndex : matchingColorCells) {
            TP.addDayOfYear(244 + cellIndex.getColumnIndex());
            TP.addTimeStart(5 + cellIndex.getRowIndex());  // 5, 6, или 7 в зависимости от индекса строки
        }

        return TP;
    }

    private static boolean colorsMatch(Color color1, Color color2) {
        return color1.getRed().equals(color2.getRed()) && color1.getGreen().equals(color2.getGreen()) && color1.getBlue().equals(color2.getBlue());
    }
    private static List<String> getSubjectsFromColumnC(String spreadsheetId) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        // Задаем начальное значение и максимальное количество строк для чтения
        int startRow = 6;
        int endRow = service.spreadsheets()
                .get(spreadsheetId)
                .execute().getSheets().get(0).getProperties().getGridProperties().getRowCount().intValue();

        List<String> subjects = new ArrayList<>();
        Set<String> knownSubjects = new HashSet<>();

        for (int i = startRow; i < endRow; i += 3) {
            String range = "C" + i + ":C" + (i + 2);
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();

            if (values != null && !values.isEmpty()) {
                if (!values.get(0).isEmpty()) {
                    String potentialSubject = values.get(0).get(0).toString();
                    if (!knownSubjects.contains(potentialSubject)) {
                        subjects.add(potentialSubject);
                        knownSubjects.add(potentialSubject);
                    }
                }
            }
        }

//        // Вывод всех найденных предметов для отладки
//        System.out.println("Found subjects: " + subjects);

        return subjects;
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    public static void main(String... args) throws IOException, GeneralSecurityException {
//        String spreadsheetId = "1pD0dce2o_BsB68hm33pSxZd4fNOFFRrxbgOvbmB7UIc";
        String spreadsheetId = "1NlUU1ulotC5Kjiz-ctVhzwyAcyEWbK2ZY5eA4Z2PKoQ";
        List<String> subjects = getSubjectsFromColumnC(spreadsheetId);
        List<Lesson> lessons = new ArrayList<>();
        for (String subject : subjects) {
            int startRow = (subjects.indexOf(subject) * 3) + 6;
            int endRow = startRow + 2;
            String range = "!G" + startRow + ":DW" + endRow;
            Lesson lesson = getScheduleForWebinar(spreadsheetId, subject, range);
            if (!lesson.getTimeStart().isEmpty()){
                lessons.add(lesson);
            }
        }

        for (Lesson lesson : lessons) {
            System.out.println("Subject: " + lesson.getName());
            System.out.println("Days of Year: " + lesson.getDayOfYear());
            System.out.println("Start Times: " + lesson.getTimeStart());
            System.out.println("-------------");
        }
    }
}
