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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 天気予報データを取得するクラスです。
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
 * 都道府県コードと名前のマッピングを提供するクラス
 */
class PrefectureMapper {
    private static final Map<String, String> PREFECTURE_MAP = new HashMap<>();

    static {
        String[][] prefectures = {
                { "010000", "北海道" }, { "020000", "青森県" }, { "030000", "岩手県" },
                { "040000", "宮城県" }, { "050000", "秋田県" }, { "060000", "山形県" },
                { "070000", "福島県" }, { "080000", "茨城県" }, { "090000", "栃木県" },
                { "100000", "群馬県" }, { "110000", "埼玉県" }, { "120000", "千葉県" },
                { "130000", "東京都" }, { "140000", "神奈川県" }, { "150000", "新潟県" },
                { "160000", "富山県" }, { "170000", "石川県" }, { "180000", "福井県" },
                { "190000", "山梨県" }, { "200000", "長野県" }, { "210000", "岐阜県" },
                { "220000", "静岡県" }, { "230000", "愛知県" }, { "240000", "三重県" },
                { "250000", "滋賀県" }, { "260000", "京都府" }, { "270000", "大阪府" },
                { "280000", "兵庫県" }, { "290000", "奈良県" }, { "300000", "和歌山県" },
                { "310000", "鳥取県" }, { "320000", "島根県" }, { "330000", "岡山県" },
                { "340000", "広島県" }, { "350000", "山口県" }, { "360000", "徳島県" },
                { "370000", "香川県" }, { "380000", "愛媛県" }, { "390000", "高知県" },
                { "400000", "福岡県" }, { "410000", "佐賀県" }, { "420000", "長崎県" },
                { "430000", "熊本県" }, { "440000", "大分県" }, { "450000", "宮崎県" },
                { "460000", "鹿児島県" }, { "470000", "沖縄県" }
        };

        for (String[] prefecture : prefectures) {
            PREFECTURE_MAP.put(prefecture[0], prefecture[1]);
        }
    }

    public String getPrefectureName(String code) {
        return PREFECTURE_MAP.getOrDefault(code, "不明な都道府県コード");
    }
}

/**
 * 天気予報アプリ -本体
 * このアプリケーションは、気象庁のWeb APIから天気予報データを取得して表示します
 * 
 * @author n.katayama
 * @version 1.0
 */
public class WeaatherForecastApp {
    /**
     * メイン処理: 天気予報の取得と表示を実行します
     * 
     * @param args コマンドライン引数(使用しません)
     */
    public static void main(String[] args) {
        PrefectureMapper mapper = new PrefectureMapper();
        WeatherDataFetcher fetcher = new WeatherDataFetcher();
        WeatherDataParser parser = new WeatherDataParser();
        WeatherDataDisplayer displayer = new WeatherDataDisplayer();

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("都道府県コードを入力してください: ");
            String prefectureCode = scanner.nextLine();
            String prefectureName = mapper.getPrefectureName(prefectureCode);
            System.out.println("都道府県名: " + prefectureName);

            if (!prefectureName.equals("不明な都道府県コード")) {
                String targetUrl = "https://www.jma.go.jp/bosai/forecast/data/forecast/" + prefectureCode + ".json";

                try {
                    String jsonData = fetcher.fetchWeatherData(targetUrl);
                    List<WeatherForecast> forecasts = parser.parseWeatherData(jsonData);
                    displayer.displayWeatherData(forecasts);
                } catch (IOException e) {
                    System.err.println("データ取得エラー: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("予期しないエラー: " + e.getMessage());
                }
            } else {
                System.out.println("無効な都道府県コードが入力されました。");
            }
        }
    }
}
