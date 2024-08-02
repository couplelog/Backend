package com.Lubee.Lubee.common.oauth;

import com.Lubee.Lubee.common.api.ApiResponseDto;
import com.Lubee.Lubee.common.api.ResponseUtils;
import com.Lubee.Lubee.common.enumSet.ErrorType;
import com.Lubee.Lubee.common.enumSet.LoginType;
import com.Lubee.Lubee.common.enumSet.UserRoleEnum;
import com.Lubee.Lubee.common.exception.RestApiException;
import com.Lubee.Lubee.common.jwt.JwtUtil;
import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.couple.repository.CoupleRepository;
import com.Lubee.Lubee.user.domain.User;
import com.Lubee.Lubee.user.dto.KakaoUserInfoDto;
import com.Lubee.Lubee.user.dto.SignupDto;
import com.Lubee.Lubee.user.dto.TokenDto;
import com.Lubee.Lubee.user.repository.UserRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthService {

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${REST_API_KEY}")           // 카카오 rest api key
    private String res_api_Key;

    @Value("admin_key")
    private String admin_key;

    public Boolean test()
    {
        return Boolean.FALSE;
    }
    /*
        카카오 로그인
     */

    // code를 넘기면 access token을 반환
    public String getKakaoAccessToken(String code) {
        String accessToken = "";
        String refreshToken = "";
        System.out.println("start");
        String reqURL = "https://kauth.kakao.com/oauth/token"; // access token을 반환해주는 주소 (카카오에서 제공하는 기능)

        try {
            URL url = new URL(reqURL); // Url 처리
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 실제 서버와의 연결을 설정 + 반환된 객체를 HttpURLConnection으로 캐스팅

            // POST 요청을 위해 기본값이 false인 setDoOutput을 true로 설정
            conn.setRequestMethod("POST");
            conn.setDoOutput(true); // 객체에서 출력 스트림을 사용할 것인지를 설정하는 메서드 -> 서버로 데이터를 보낼 때 사용

            // POST 요청에 필요로 요구하는 파라미터를 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter((new OutputStreamWriter(conn.getOutputStream()))); // 전송하기 위한 것
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=").append(res_api_Key); // 배포 하고 나서 설정
            sb.append("&redirect_uri=https://www.lubee.site/api/users/kakao/simpleLogin");
            //sb.append("&redirect_uri=https://lubee.site/api/users/kakao/simpleLogin"); // 이부분 나중에 변경해야함
            //sb.append("&redirect_uri=http://localhost:8080/api/users/kakao/simpleLogin"); // 로컬로 돌릴 때
            //sb.append("&redirect_uri=http://localhost:5173/api/users/kakao/simpleLogin"); // 프론트로 돌릴 때
            sb.append("&client_secret=b3duTXcnAuFZVjHfVNAIgREK3yZWNOvd");
            sb.append("&code=").append(code);
            System.out.println("sb : " +sb);
            bw.write(sb.toString());
            bw.flush();

            // 결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            String result = new String();

            if (responseCode >= 200 && responseCode < 300) {
                // 성공적인 응답
                // 서버가 응답한 데이터를 읽어오기
                InputStream inputStream = conn.getInputStream();
                // InputStream을 문자열로 변환
                result = new BufferedReader(new InputStreamReader(inputStream))
                        // 스트림에서 읽은 각 라인들을 개행문자열로 연결하여 하나의 문자열로 만든다
                        .lines().collect(Collectors.joining("\n"));

                System.out.println("result = " + result);
            } else {
                // 에러 응답
                InputStream errorStream = conn.getErrorStream();
                // 에러 스트림을 문자열로 변환
                String errorResult = new BufferedReader(new InputStreamReader(errorStream))
                        .lines().collect(Collectors.joining("\n"));

                System.out.println("Error result = " + errorResult);
            }

            // Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    // access token을 넘기면 kakao user 정보를 반환
    public KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        KakaoUserInfoDto kakaoUserInfoDto = new KakaoUserInfoDto();

        // access_token을 이용하여 사용자 정보 조회
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken); //전송할 header 작성, access_token전송
            System.out.println("conn" + conn);
            //결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            String result = new String();

            if (responseCode >= 200 && responseCode < 300) {
                // 성공적인 응답
                InputStream inputStream = conn.getInputStream();
                // InputStream을 문자열로 변환
                result = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining("\n"));

                System.out.println("result = " + result);
            } else {
                // 에러 응답
                InputStream errorStream = conn.getErrorStream();
                // 에러 스트림을 문자열로 변환
                String errorResult = new BufferedReader(new InputStreamReader(errorStream))
                        .lines().collect(Collectors.joining("\n"));

                System.out.println("Error result = " + errorResult);
            }

            //Gson 라이브러리로 JSON파싱
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            JsonElement kakaoAccount = element.getAsJsonObject().get("kakao_account");
            JsonElement profile = kakaoAccount.getAsJsonObject().get("profile");

            //dto에 저장하기
            kakaoUserInfoDto.setKakaoId(element.getAsJsonObject().get("id").getAsString());
            kakaoUserInfoDto.setNickname(profile.getAsJsonObject().get("nickname").getAsString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return kakaoUserInfoDto;
    }


    // refresh token으로 access token 재발급
    @Transactional
    public ApiResponseDto<TokenDto> refreshKakaoToken(String accessTokenOrigin, String refreshTokenOrigin) {

        String accessToken = accessTokenOrigin.substring(7);
        String refreshToken = refreshTokenOrigin.substring(7);

        // 아직 만료되지 않은 토큰으로는 refresh 할 수 없음
        if (jwtUtil.validateToken(accessToken)) throw new RestApiException(ErrorType.ACCESS_TOKEN_NOT_EXPIRED);

        Claims claims = jwtUtil.getUserInfoFromToken(refreshToken);
        String username = claims.get("sub", String.class); // access token에서 username을 가져옴

        System.out.println(username);

        User user = userRepository.findByUsernameAndLoginType(username, LoginType.KAKAO)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));

        String kakaoRefreshToken = user.getKakaoRefreshToken();

        if (!jwtUtil.validateToken(kakaoRefreshToken) || !refreshToken.equals(kakaoRefreshToken)) {
            throw new RestApiException(ErrorType.REFRESH_TOKEN_NOT_VALIDATE); // 만료되거나 일치하지 않는 리프레시 토큰은 에러처리
        }

        String newAccessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole());

        TokenDto tokenDto = new TokenDto();
        tokenDto.setMessage("access token 재발급 성공");
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(refreshTokenOrigin);

        return ResponseUtils.ok(tokenDto, null);
    }

    @Transactional
    public ApiResponseDto<TokenDto> kakaoLoginOrSignupSimple(String code) {

        String kakaoAccessToken = getKakaoAccessToken(code);
        KakaoUserInfoDto kakaoUserInfoDto = getKakaoUserInfo(kakaoAccessToken); // 카카오 유저 정보 받아오기

        System.out.println("kakaoUserInfoDto: " + kakaoUserInfoDto);
        // 아이디, 비밀번호 받아오기
        String username = kakaoUserInfoDto.getKakaoId();
        String password = passwordEncoder.encode("kakaouserpassword"); // 카카오 유저 비밀번호 임의 설정
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        // 회원 아이디 중복 확인 -> DB에 존재하지 않으면 회원가입 수행
        Optional<User> user = userRepository.findByUsernameAndLoginType(username, LoginType.KAKAO);
        TokenDto tokenDto = new TokenDto();
        // 회원가입 온보딩을 한다.
        if (user.isEmpty()) {
            // 입력한 username, password, admin 으로 user 객체 만들어 repository 에 저장
            UserRoleEnum role = UserRoleEnum.USER; // 카카오 유저 ROLE 임의 설정
            User signUpUser = User.of(LoginType.KAKAO, username, password, role);
            signUpUser.setLoginType(LoginType.KAKAO);


            String accessToken = jwtUtil.createAccessToken(signUpUser.getUsername(), signUpUser.getRole());
            String refreshToken = jwtUtil.createRefreshToken(signUpUser.getUsername(), signUpUser.getRole());
            String accessTokenWithoutBearer = accessToken.substring(7);
            String refreshTokenWithoutBearer = refreshToken.substring(7);
            System.out.println("access Token : " + accessTokenWithoutBearer);
            System.out.println("refresh Token : " + refreshTokenWithoutBearer);

            // refresh token을 DB에 저장
            signUpUser.setKakaoRefreshToken(refreshTokenWithoutBearer);
/*
            Couple couple = new Couple(signUpUser, null);
            coupleRepository.save(couple);
            signUpUser.setCouple(couple);*/

            userRepository.save(signUpUser);
            // response 생성
            tokenDto.setMessage("카카오 회원가입 절반 성공, 온보딩으로 이동!");
            tokenDto.setAccessToken(accessTokenWithoutBearer);
            tokenDto.setRefreshToken(refreshTokenWithoutBearer);


        } else { // DB에 존재하면 로그인 수행
            // 토큰 생성

            String accessToken = jwtUtil.createAccessToken(user.get().getUsername(), user.get().getRole());
            String refreshToken = jwtUtil.createRefreshToken(user.get().getUsername(), user.get().getRole());
            String accessTokenWithoutBearer = accessToken.substring(7);
            String refreshTokenWithoutBearer = refreshToken.substring(7);
            // refresh token을 DB에 저장
            user.get().setKakaoRefreshToken(refreshTokenWithoutBearer);
            userRepository.save(user.get());

            // response 생성
            tokenDto.setMessage("카카오 로그인 성공");
            tokenDto.setAccessToken(accessTokenWithoutBearer);
            tokenDto.setRefreshToken(refreshTokenWithoutBearer);


        }
        return ResponseUtils.ok(tokenDto, null);
    }


}
