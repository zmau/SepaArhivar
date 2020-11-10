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
import java.util.Arrays;
import java.util.List;

public class SheetWriter {
    //https://www.baeldung.com/google-sheets-java-client
    private static final String APPLICATION_NAME = "SEPA Arhivar";
    private static String SPREADSHEET_ID = "1d-OPrhCoqKUSCu9wXbOIfWmcw-sFh1SWru26sJFQpZw";
    private static String API_KEY = "AIzaSyAGp4Y2SS9mLyTMZWrhZ4YGET-NgnUjDQw";
    private static Sheets sheetsService;

    public SheetWriter() throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();
    }
    private static final String CREDENTIALS_FILE_PATH = "C:\\dev\\SepaArhivar\\credentials.json";
    //private static final String CREDENTIALS_FILE_PATH = "F:\\SepaArchiver\\credentials.json";

    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();

        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential   )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    public static Credential authorize() throws IOException, GeneralSecurityException {
        // InputStream in = GoogleAuthorizeUtil.class.getResourceAsStream("/google-sheets-client-secret.json");
        File initialFile = new File(CREDENTIALS_FILE_PATH);
        InputStream in = new FileInputStream(initialFile);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), clientSecrets, scopes).setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        return credential;

    }

    public void WriteItem(DataItem item) throws IOException {
        List<Object> itemAsObjets = Arrays.asList(item.getDate(), item.getHour()
                , item.getSO2AsString(), item.getPM10AsString(), item.getNO2AsString(), item.getCOAsString(), item.getPM2comma5AsString());
        ValueRange body = new ValueRange().setValues(Arrays.asList(itemAsObjets));

        AppendValuesResponse appendResult = sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, "A1", body)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }
}