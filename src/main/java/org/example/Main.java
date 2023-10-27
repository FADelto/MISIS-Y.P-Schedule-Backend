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

    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

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


    // Получение расписания для 1 предмета
    public static Lesson getScheduleForWebinar( String spreadsheetId, String subjectName, String range) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Spreadsheet response1 = service.spreadsheets()
                .get(spreadsheetId)
                .setRanges(Collections.singletonList(range))
                .setIncludeGridData(true)
                .execute();

        List<Integer> list = new ArrayList<>();
        List<Integer> list1 = new ArrayList<>();
        Lesson lesson = new Lesson(subjectName, list, list1);

        // Проверка на семестр для определения дня в году
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("D");
        int dayOfYear = Integer.parseInt(formatter.format(date));
        if (dayOfYear < 200) {
            dayOfYear = 0;
        } else dayOfYear = 244;

        // Проверка на количество строк для определения номера пары
        String[][] checkRange = new String[][]{range.split(":DW")};
        int startRange = Integer.parseInt(checkRange[0][1]);
        int endRange = Integer.parseInt(checkRange[0][0].split("!G")[1]);

        // Создание массива для проверки на тип урока
        List<Color> targetColors = new ArrayList<>();
        targetColors.addAll(LessonType.ONLINE.getColor());
        targetColors.addAll(LessonType.LECTURE.getColor());
        targetColors.addAll(LessonType.GROUP1.getColor());
        targetColors.addAll(LessonType.GROUP2.getColor());

        // Парсинг таблицы
        if (response1 != null && !response1.getSheets().isEmpty()) { // Проверка ответа от сервера
            Sheet sheet = response1.getSheets().get(0);
            if (sheet.getData() != null && !sheet.getData().isEmpty()) { // Проверка заполнености таблицы
                List<RowData> rowDataList = sheet.getData().get(0).getRowData();
                for (int rowIndex = 0; rowIndex < rowDataList.size(); rowIndex++) { // Парсинг строк
                    RowData rowData = rowDataList.get(rowIndex);
                    for (int columnIndex = 0; columnIndex < rowData.getValues().size(); columnIndex++) { // Парсинг столбцов
                        CellData cell = rowData.getValues().get(columnIndex);
                        if (cell.getUserEnteredFormat() != null && cell.getUserEnteredFormat().getBackgroundColor() != null) { // Проверка на наличие цвета
                            Color cellColor = cell.getUserEnteredFormat().getBackgroundColor();
                            for(Color targetcolor : targetColors) {
                                if (cellColor.getRed().equals(targetcolor.getRed()) &&
                                     cellColor.getGreen().equals(targetcolor.getGreen()) &&
                                     cellColor.getBlue().equals(targetcolor.getBlue()) ) {
                                    if (cell.getFormattedValue()!= null){ // Проверка на ошибки (В таблице есть пустые ячейки с нужным фоном)
                                        lesson.addLessonType(LessonType.values()[targetColors.indexOf(targetcolor)]);
                                        lesson.addDayOfYear(dayOfYear + columnIndex);
                                        if (endRange - startRange == 4) {
                                            if (rowIndex > 1) {
                                                lesson.addTimeStart(3 + rowIndex);
                                            }
                                            else {
                                                lesson.addTimeStart(2 + rowIndex);
                                            }
                                        }
                                        else {
                                            lesson.addTimeStart(5 + rowIndex); // 5, 6, или 7 в зависимости от индекса строки
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return lesson;
    }

    // Получение всех названий предметов и их диапазон строк
    private static List<SubjectI> getSubjectsFromColumnC(String spreadsheetId) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Получаем информацию о таблице
        Spreadsheet data = service.spreadsheets()
                                  .get(spreadsheetId)
                                  .setIncludeGridData(true)
                                  .execute();

        // Задаем начальное значение и максимальное количество строк для чтения
        int startRow = 6;
        int endRow = data.getSheets().get(0).getProperties().getGridProperties().getRowCount();

        // Получаем информацию о строках для проверки на скрытые строки
        List<DimensionProperties> rowdata = data.getSheets().get(0).getData().get(0).getRowMetadata();

        // Получаем названия предметов
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, "C" + startRow + ":C" + endRow)
                .execute();

        List<List<Object>> values = response.getValues();
        List<SubjectI> subjectIList = new ArrayList<>();
        SubjectI subjectIListItem = new SubjectI();

        for (int i=0; i<values.size(); i++) {
            if (rowdata.get(5+i).size() == 1) {
                if (!values.get(i).isEmpty() && i == 0) {
                    subjectIListItem = new SubjectI(values.get(i).get(0).toString(), new int[]{6 + i, 6 + i});
                }
                if (!subjectIListItem.isEmpty() && values.get(i).isEmpty()) {
                    subjectIListItem.incrementEndRowCounter();
                }
                if (!subjectIListItem.isEmpty() && !values.get(i).isEmpty() && i != 0) {
                    subjectIList.add(subjectIListItem);
                    subjectIListItem = new SubjectI(values.get(i).get(0).toString(), new int[]{6 + i, 6 + i});
                }
                if (i == values.size() - 1) {
                    subjectIList.add(subjectIListItem);
                }
            }
        }

        return subjectIList;
    }

    //Основной метод для получения расписания
    public static List<Lesson> getSchedule(String spreadsheetId) throws GeneralSecurityException, IOException {
        List<SubjectI> subjects = getSubjectsFromColumnC(spreadsheetId);
        List<Lesson> lessons = new ArrayList<>();
        for (SubjectI subject : subjects) {
            int startRow = subject.getRange()[0];
            int endRow = subject.getRange()[1];
            String range = "!G" + startRow + ":DW" + endRow;
            Lesson lesson = getScheduleForWebinar(spreadsheetId, subject.getName(), range);
            if (!lesson.getTimeStart().isEmpty()){
                lessons.add(lesson);
            }
        }
        return lessons;
    }
    public static void main(String... args) throws GeneralSecurityException, IOException {
//        String spreadsheetId = "1pD0dce2o_BsB68hm33pSxZd4fNOFFRrxbgOvbmB7UIc";
        String spreadsheetId = "1NlUU1ulotC5Kjiz-ctVhzwyAcyEWbK2ZY5eA4Z2PKoQ";
        List<Lesson> lessons = getSchedule(spreadsheetId);

        for (Lesson lesson : lessons) {
            System.out.println("Subject: " + lesson.getName());
            System.out.println("Days of Year: " + lesson.getDayOfYear());
            System.out.println("Start Times: " + lesson.getTimeStart());
            System.out.println("Lesson Type: " + lesson.getLessonType());
            System.out.println("-------------");
        }
    }
}
