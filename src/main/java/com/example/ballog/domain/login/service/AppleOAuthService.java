package com.example.ballog.domain.login.service;

import com.example.ballog.domain.login.dto.response.AppleResponse;
import com.example.ballog.domain.login.entity.OAuthToken;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.OAuthTokenRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemObject;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AppleOAuthService {

    private final UserRepository userRepository;
    private final OAuthTokenRepository oAuthTokenRepository;

    @Value("${apple.team-id}")
    private String appleTeamId;

    @Value("${apple.key-id}")
    private String appleLoginKeyId;

    @Value("${apple.key-path}")
    private String appleKeyPath;

    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String appleClientId;

    @Value("${spring.security.oauth2.client.registration.apple.redirect-uri}")
    private String appleUri;


    private final static String APPLE_AUTH_URL = "https://appleid.apple.com";

    public String getAppleLogin() {
        return APPLE_AUTH_URL + "/auth/authorize"
                + "?client_id=" + appleClientId
                + "&redirect_uri=" + appleUri
                + "&response_type=code%20id_token&scope=name%20email&response_mode=form_post";
    }

    public AppleResponse getAppleInfo(String code) throws Exception{
        if(code == null)
            throw new Exception("인가코드를 가져오는데 실패");

        String clientSecret = createClientSecret();
        String userId = "";
        String email  = "";
        String accessToken = "";
        String refreshToken="";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded");

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type"   , "authorization_code");
            params.add("client_id"    , appleClientId);
            params.add("client_secret", clientSecret);
            params.add("code"         , code);
            params.add("redirect_uri" , appleUri);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    APPLE_AUTH_URL + "/auth/token",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(response.getBody());

            accessToken = String.valueOf(jsonObj.get("access_token"));
            refreshToken = String.valueOf(jsonObj.get("refresh_token"));

            SignedJWT signedJWT = SignedJWT.parse(String.valueOf(jsonObj.get("id_token")));
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            userId = claimsSet.getSubject();
            email = claimsSet.getStringClaim("email");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("API 가져오기 실패");
        }
        return new AppleResponse(userId, accessToken,refreshToken, email);
    }

    @Transactional
    public void saveAppleToken(User user, AppleResponse appleResponse) {
        User savedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

        OAuthToken token = oAuthTokenRepository.findByUser(savedUser)
                .orElse(new OAuthToken());

        token.setUser(savedUser);
        token.setProvider("Apple");
        token.setProviderId(appleResponse.getId());
        token.setAccessToken(appleResponse.getAccessToken());
        token.setRefreshToken(appleResponse.getRefreshToken());

        oAuthTokenRepository.save(token);
    }


    private String createClientSecret() throws Exception {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(appleLoginKeyId)
                .type(JOSEObjectType.JWT)
                .build();

        Date now = new Date();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(appleTeamId)
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + 3600000))
                .audience(APPLE_AUTH_URL)
                .subject(appleClientId)
                .build();

        SignedJWT jwt = new SignedJWT(header, claimsSet);

        try {
            byte[] privateKeyBytes = getPrivateKey();
            ECPrivateKey privateKey = getECPrivateKey(privateKeyBytes);
            JWSSigner jwsSigner = new ECDSASigner(privateKey);
            jwt.sign(jwsSigner);
        } catch (InvalidKeyException | JOSEException e) {
            throw new Exception("client secret 생성 실패", e);
        }

        return jwt.serialize();
    }
    public byte[] getPrivateKey() throws Exception {
        if (appleKeyPath == null || appleKeyPath.isEmpty()) {
            throw new Exception("애플 개인 키가 설정되어 있지 않습니다.");
        }

        String privateKeyPem = appleKeyPath;
        if (privateKeyPem.startsWith("\"") && privateKeyPem.endsWith("\"")) {
            privateKeyPem = privateKeyPem.substring(1, privateKeyPem.length() - 1);
        }

        privateKeyPem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""); // 공백 제거

        return Base64.getDecoder().decode(privateKeyPem);
    }



    public ECPrivateKey getECPrivateKey(byte[] privateKeyBytes) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return (ECPrivateKey) kf.generatePrivate(keySpec);
    }


    //애플 로그인 연결 끊기
    public void logoutFromApple(String refreshToken) {
        String uriStr = "https://appleid.apple.com/auth/revoke";

        try {
            String clientSecret = createClientSecret();

            Map<String, String> params = new HashMap<>();
            params.put("client_id", appleClientId);
            params.put("client_secret", clientSecret);
            params.put("token", refreshToken);
            params.put("token_type_hint", "refresh_token");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uriStr))
                    .POST(buildFormData(params))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new CustomException(ErrorCode.APPLE_REVOKE_FAILED);
            }

        } catch (Exception e) {
            throw new CustomException(ErrorCode.APPLE_REVOKE_FAILED, e.getMessage());
        }
    }

    private static HttpRequest.BodyPublisher buildFormData(Map<String, String> data) {
        StringBuilder form = new StringBuilder();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (form.length() > 0) form.append("&");
            form.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            form.append("=");
            form.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return HttpRequest.BodyPublishers.ofString(form.toString());
    }
}
