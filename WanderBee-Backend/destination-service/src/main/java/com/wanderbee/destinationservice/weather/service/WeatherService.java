package com.wanderbee.destinationservice.weather.service;

import com.wanderbee.destinationservice.weather.client.WeatherClient;
import com.wanderbee.destinationservice.weather.dto.DailyWeather;
import com.wanderbee.destinationservice.weather.dto.FiveDayForecastResponse;
import com.wanderbee.destinationservice.weather.dto.ForecastItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.*;


@Service
public class WeatherService {

    private final WeatherClient weatherClient;

    @Value("${openweather.api.key}")
    private String apiKey;

    public WeatherService(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @Cacheable(value = "weatherForecast", key = "#cityName")
    public List<DailyWeather> getDailyForecast(String cityName) {
        // 1. Fetch raw data
        FiveDayForecastResponse response = weatherClient.getFiveDayForecast(cityName, apiKey, "metric");

        // 2. Group by Date (yyyy-MM-dd) using the .list() accessor
        Map<String, List<ForecastItem>> groupedByDate = response.list().stream()
                .collect(Collectors.groupingBy(item -> item.dtTxt().substring(0, 10)));

        String today = LocalDate.now().toString();

        // 3. Process each group
        return groupedByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String dateStr = entry.getKey();
                    List<ForecastItem> dayForecasts = entry.getValue();

                    // Using .main() and .tempMin()/.tempMax() accessors
                    int minTemp = dayForecasts.stream()
                            .mapToInt(f -> (int) f.main().tempMin())
                            .min().orElse(0);
                    int maxTemp = dayForecasts.stream()
                            .mapToInt(f -> (int) f.main().tempMax())
                            .max().orElse(0);

                    // Find noon representative using .dtTxt()
                    ForecastItem representative = dayForecasts.stream()
                            .min(Comparator.comparingInt(f -> {
                                int hour = Integer.parseInt(f.dtTxt().substring(11, 13));
                                return Math.abs(hour - 12);
                            })).orElse(dayForecasts.get(0));

                    boolean isToday = dateStr.equals(today);
                    String dayLabel = isToday ? "Today" :
                            LocalDate.parse(dateStr).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

                    // Using .weather() and record accessors for the final DTO
                    return new DailyWeather(
                            dayLabel,
                            minTemp,
                            maxTemp,
                            representative.weather().get(0).icon(),
                            representative.weather().get(0).main(),
                            isToday
                    );
                })
                .limit(5)
                .collect(Collectors.toList());
    }
}