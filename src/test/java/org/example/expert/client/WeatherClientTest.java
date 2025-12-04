package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WeatherClientTest {

    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private WeatherClient weatherClient;

    @Test
    void 날씨_조회_시_응답_상태가_OK가_아니면_예외가_발생한다() {
        // given
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).willReturn(responseEntity);

        // when & then
        ServerException exception = assertThrows(ServerException.class, () ->
            weatherClient.getTodayWeather()
        );
        assertTrue(exception.getMessage().contains("날씨 데이터를 가져오는데 실패했습니다"));
    }

    @Test
    void 날씨_조회_시_응답_바디가_null이면_예외가_발생한다() {
        // given
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).willReturn(responseEntity);

        // when & then
        ServerException exception = assertThrows(ServerException.class, () ->
            weatherClient.getTodayWeather()
        );
        assertEquals("날씨 데이터가 없습니다.", exception.getMessage());
    }

    @Test
    void 날씨_조회_시_응답_바디가_비어있으면_예외가_발생한다() {
        // given
        WeatherDto[] emptyArray = new WeatherDto[0];
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(emptyArray, HttpStatus.OK);
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).willReturn(responseEntity);

        // when & then
        ServerException exception = assertThrows(ServerException.class, () ->
            weatherClient.getTodayWeather()
        );
        assertEquals("날씨 데이터가 없습니다.", exception.getMessage());
    }

    @Test
    void 날씨_조회_시_오늘_날짜에_해당하는_데이터가_없으면_예외가_발생한다() {
        // given
        WeatherDto[] weatherArray = {
            new WeatherDto("01-01", "Sunny"),
            new WeatherDto("02-02", "Rainy")
        };
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(weatherArray, HttpStatus.OK);
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).willReturn(responseEntity);

        // when & then
        ServerException exception = assertThrows(ServerException.class, () ->
            weatherClient.getTodayWeather()
        );
        assertEquals("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 날씨_조회가_정상적으로_처리된다() {
        // given
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
        WeatherDto[] weatherArray = {
            new WeatherDto(today, "Sunny"),
            new WeatherDto("01-01", "Rainy")
        };
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(weatherArray, HttpStatus.OK);
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).willReturn(responseEntity);

        // when
        String weather = weatherClient.getTodayWeather();

        // then
        assertNotNull(weather);
        assertEquals("Sunny", weather);
    }
}
