import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 天気予報データを取得するクラスz
 */
class WeatherDataFetcher {
    public String fetchWeatherData(String targetUrl) throws IOException {
        URI uri = URI.create(targetUrl);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
            }
            return responseBody.toString();
        } else {
            throw new IOException("データ取得に失敗しました: HTTP " + responseCode);
        }
    }
}

/**
 * 天気予報データを解析するクラス
 */
class WeatherDataParser {
    public List<WeatherForecast> parseWeatherData(String jsonData) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        JSONArray rootArray = new JSONArray(jsonData);
        JSONObject timeStringObject = rootArray.getJSONObject(0)
                .getJSONArray("timeSeries").getJSONObject(0);

        JSONArray timeDefinesArray = timeStringObject.getJSONArray("timeDefines");
        JSONArray weathersArray = timeStringObject.getJSONArray("areas")
                .getJSONObject(0).getJSONArray("weathers");

        for (int i = 0; i < timeDefinesArray.length(); i++) {
            LocalDateTime dateTime = LocalDateTime.parse(
                    timeDefinesArray.getString(i),
                    DateTimeFormatter.ISO_DATE_TIME);
            String weather = weathersArray.getString(i);
            forecasts.add(new WeatherForecast(dateTime, weather));
        }

        return forecasts;
    }
}

/**
 * 天気予報データを表すクラス
 */
class WeatherForecast {
    private final LocalDateTime dateTime;
    private final String weather;

    public WeatherForecast(LocalDateTime dateTime, String weather) {
        this.dateTime = dateTime;
        this.weather = weather;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getWeather() {
        return weather;
    }
}

/**
 * 天気予報データを表示するクラス
 */
class WeatherDataDisplayer {
    public void displayWeatherData(List<WeatherForecast> forecasts) {
        for (WeatherForecast forecast : forecasts) {
            System.out.println(forecast.getDateTime().format(
                    DateTimeFormatter.ofPattern("yyyy/MM/dd")) + " " + forecast.getWeather());
        }
    }
}

/**
 * 天気予報アプリ -本体
 * このアプリケーションは、気象庁のWeb APIから大阪府の天気予報データを取得して表示します
 * 
 * @author n.katayama
 * @version 1.0
 */
public class WeaatherForecastApp {
    /**
     * 気象庁の天気予報APIのエンドポイントURL
     * 大阪府の天気予報データを提供します
     */
    private static final String TARGET_URL = "https://www.jma.go.jp/bosai/forecast/data/forecast/270000.json";

    /**
     * メイン処理: 天気予報の取得と表示を実行します
     * 
     * @param args コマンドライン引数(使用しません)
     */
    public static void main(String[] args) {
        WeatherDataFetcher fetcher = new WeatherDataFetcher();
        WeatherDataParser parser = new WeatherDataParser();
        WeatherDataDisplayer displayer = new WeatherDataDisplayer();

        try {
            String jsonData = fetcher.fetchWeatherData(TARGET_URL);
            List<WeatherForecast> forecasts = parser.parseWeatherData(jsonData);
            displayer.displayWeatherData(forecasts);
        } catch (IOException e) {
            System.err.println("データ取得エラー: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("予期しないエラー: " + e.getMessage());
        }
    }
}
