package com.temporary.scheduleapi;

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
import com.temporary.scheduleapi.Repos.LessonRepo;
import com.temporary.scheduleapi.models.Lesson;
import com.temporary.scheduleapi.models.LessonType;
import com.temporary.scheduleapi.models.SubjectI;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ScheduleParse {
    private final LessonRepo lessonRepo;

    public ScheduleParse(LessonRepo lessonRepo) {
        this.lessonRepo = lessonRepo;
    }

//    @Value("${credentials.file.path}")
//    private static String CREDENTIALS_FILE_PATH;
    private static final String APPLICATION_NAME = "ScheduleParser";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "authFiles/credentials.json";


    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = ScheduleParse.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
    public void getScheduleForWebinar(String spreadsheetId, SubjectI subjectI) throws RuntimeException, IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Получение пар
        String range = "!G" + subjectI.getRange()[0] + ":GZ" + subjectI.getRange()[1];
        Spreadsheet response = service.spreadsheets()
                .get(spreadsheetId)
                .setRanges(Collections.singletonList(range))
                .setIncludeGridData(true)
                .execute();

        // Получение номера пары
        List<List<Object>> responseFromColumnsEF = service.spreadsheets().values()
                .get(spreadsheetId, "E" + subjectI.getRange()[0] + ":F" + subjectI.getRange()[1])
                .execute().getValues();

        // Проверка на семестр для определения дня в году
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("D");
        int dayOfYear = Integer.parseInt(formatter.format(date));
        if (dayOfYear < 200) {
            dayOfYear = 0;
        } else dayOfYear = 244;

        // Создание массива для проверки на тип урока
        List<Color> targetColors = new ArrayList<>();
        targetColors.addAll(LessonType.ONLINE.getColor());
        targetColors.addAll(LessonType.GROUP1.getColor());
        targetColors.addAll(LessonType.GROUP2.getColor());
        targetColors.addAll(LessonType.LECTURE.getColor());

        // Парсинг таблицы
        if (response != null && !response.getSheets().isEmpty()) { // Проверка ответа от сервера
            Sheet sheet = response.getSheets().get(0);
            if (sheet.getData() == null || sheet.getData().isEmpty()) { // Проверка заполнености таблицы
                throw new RuntimeException();
            }
            List<RowData> rowDataList = sheet.getData().get(0).getRowData();
            for (int rowIndex = 0; rowIndex < rowDataList.size(); rowIndex++) { // Парсинг строк
                RowData rowData = rowDataList.get(rowIndex);
                for (int columnIndex = 0; columnIndex < rowData.getValues().size(); columnIndex++) { // Парсинг столбцов
                    CellData cell = rowData.getValues().get(columnIndex);
                    if (cell.getUserEnteredFormat() != null && cell.getUserEnteredFormat().getBackgroundColor() != null) { // Проверка на наличие цвета
                        Color cellColor = cell.getUserEnteredFormat().getBackgroundColor();
                        for (Color targetcolor : targetColors) {
                            if (cellColor.getRed().equals(targetcolor.getRed()) &&
                                    cellColor.getGreen().equals(targetcolor.getGreen()) &&
                                    cellColor.getBlue().equals(targetcolor.getBlue())) {
                                //if (cell.getFormattedValue() != null) { // Проверка на ошибки (В таблице есть пустые ячейки с нужным фоном)(Эти пустые ячейки это пары)
                                LessonType lessonTypeBuf;
                                int timeStartBuf = 0;

                                // Получение типа урока
                                if (targetColors.indexOf(targetcolor) < 3) lessonTypeBuf = LessonType.ONLINE;
                                else lessonTypeBuf = LessonType.LECTURE;

                                // Получение времени
                                if (responseFromColumnsEF.get(rowIndex).get(0).toString().isEmpty()) {
                                    switch (responseFromColumnsEF.get(rowIndex).get(1).toString()) {
                                        case ("10:00 - 11:30") -> timeStartBuf = 2;
                                        case ("11:30 - 13:00") -> timeStartBuf = 3;
                                        case ("14:00 - 15:00") -> timeStartBuf = 4;
                                        case ("16:20 - 17:55") -> timeStartBuf = 5;
                                        case ("18:00 - 19:25") -> timeStartBuf = 6;
                                        case ("19:25 - 21:00") -> timeStartBuf = 7;
                                    }
                                } else {
                                    timeStartBuf = Integer.parseInt((String) responseFromColumnsEF.get(rowIndex).get(0));
                                }
                                lessonRepo.save(new Lesson(subjectI.getName(), subjectI.getSubgroupNumber(), timeStartBuf, dayOfYear + columnIndex, lessonTypeBuf));
                                //}
                            }
                        }
                    }
                }
            }
        }
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
        boolean firstEnglishFilled = false;
        for (int i = 0; i < values.size(); i++) {
            if (rowdata.get(5 + i).size() == 1) {
                if (!values.get(i).isEmpty() && i == 0) {
                    subjectIListItem = new SubjectI(values.get(i).get(0).toString().split("\\n")[0], new int[]{6 + i, 6 + i});
                }
                if (!subjectIListItem.isEmpty() && values.get(i).isEmpty()) {
                    subjectIListItem.incrementEndRowCounter();
                }
                if (!subjectIListItem.isEmpty() && !values.get(i).isEmpty() && i != 0) {
                    subjectIList.add(subjectIListItem);
                    if (values.get(i).get(0).toString().contains("Английский")) {
                        if (firstEnglishFilled) {
                            subjectIListItem = new SubjectI(values.get(i).get(0).toString().split("\\n")[0], 1, new int[]{12, 13});
                            subjectIList.add(subjectIListItem);
                            subjectIListItem = new SubjectI(values.get(i).get(0).toString().split("\\n")[0], 4, new int[]{14, 14});
                            i += 2;

                        } else {
                            subjectIListItem = new SubjectI(values.get(i).get(0).toString().split("\\n")[0], 23, new int[]{6 + i, 6 + i});
                            firstEnglishFilled = true;
                        }
                    } else {
                        subjectIListItem = new SubjectI(values.get(i).get(0).toString().split("\\n")[0], new int[]{6 + i, 6 + i});
                    }
                }
                if (i == values.size() - 1) {
                    subjectIList.add(subjectIListItem);
                }
            }
        }
        return subjectIList;
    }

    public void saveSchedule(String spreadsheetId) throws GeneralSecurityException, IOException {
        List<SubjectI> subjects = getSubjectsFromColumnC(spreadsheetId);
        for (SubjectI subjectI : subjects) {
            getScheduleForWebinar(spreadsheetId, subjectI);
        }
    }

}