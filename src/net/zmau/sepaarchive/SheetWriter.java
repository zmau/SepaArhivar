package net.zmau.sepaarchive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//https://www.baeldung.com/google-sheets-java-client
public class SheetWriter {
    private static final String APPLICATION_NAME = "SEPA Arhivar";
    private static final String SPREADSHEET_ID = "1d-OPrhCoqKUSCu9wXbOIfWmcw-sFh1SWru26sJFQpZw";
    private static final String TEST_SPREADSHEET_ID = "13HUUuESx75JWRNeUU5FC_r3WkYeOiUviP92i7wqPtZQ";
    private static final String API_KEY = "AIzaSyAGp4Y2SS9mLyTMZWrhZ4YGET-NgnUjDQw";
    private static final String CREDENTIALS_FILE_PATH = "C:\\dev\\SepaArhivar\\credentials.json";
    //private static final String CREDENTIALS_FILE_PATH = "F:\\SepaArchiver\\credentials.json";

    private static Sheets sheetsService;
    private LocalDateTime lastObservationTime;
    private static String spreadsheetToUseID;

    public SheetWriter() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        sheetsService =  new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential   )
                .setApplicationName(APPLICATION_NAME)
                .build();
        spreadsheetToUseID = /*TEST_*/SPREADSHEET_ID;
    }

    public static Credential authorize() throws IOException, GeneralSecurityException {
        // InputStream in = GoogleAuthorizeUtil.class.getResourceAsStream("/google-sheets-client-secret.json");
        File initialFile = new File(CREDENTIALS_FILE_PATH);
        InputStream in = new FileInputStream(initialFile);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));
        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), clientSecrets, scopes).setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline").build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
}

    public LocalDateTime getLastObservationTime() throws IOException {
        List<List<Object>> observationTimeList = sheetsService.spreadsheets().values().get(spreadsheetToUseID, "A1:C").execute().getValues();
        List<Object> lastObservation = observationTimeList.get(observationTimeList.size()-1);
        String lastTimeAsString = lastObservation.get(0).toString() + " " + lastObservation.get(1);
        try {
            return LocalDateTime.parse(lastTimeAsString, DateTimeFormatter.ofPattern("dd.MM.yyyy H"));
        }
        catch (DateTimeParseException e){ // nothing already written, set LastObservationTime to deep past so everything is newer than it
            return LocalDateTime.of(1970, 1, 1, 1, 0);
        }
    }

    public void writeTheDay(List<HourDataItem> dailyObservationList) throws IOException {
        LocalDateTime lastObservationTime = getLastObservationTime();

        List<List<Object>> observationListAsObjects = new ArrayList<List<Object>>();
        for (HourDataItem observation : dailyObservationList){
            if(observation.isAfter(lastObservationTime)) {
                List<Object> observationAsObjects = Arrays.asList(
                        observation.getDate(),
                        observation.getHour(),
                        observation.getSO2AsString(),
                        observation.getPM10AsString(),
                        observation.getNO2AsString(),
                        observation.getCOAsString(),
                        observation.getPM2comma5AsString()
                );
                observationListAsObjects.add(observationAsObjects);
            }
        }
        ValueRange body = new ValueRange().setValues(observationListAsObjects);
        AppendValuesResponse appendResult = sheetsService.spreadsheets().values()
                .append(spreadsheetToUseID, "A1", body)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }
}