package com.sample.login;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface KakaoApi {
    public static final String API_URL="https://dapi.kakao.com/";

    @GET("v2/local/search/keyword.xml")
    public Call<ResponseBody> getSearchKeyword(
            @Header("Authorization") String key,
            @Query("query") String query,
            @Query("y") String y,
            @Query("x") String x,
            @Query("radius") int radius
    );

    @GET("v2/local/search/keyword.xml")
    public Call<ResponseBody> getSearchOnlyKeyword(
            @Header("Authorization") String key,
            @Query("query") String query
    );
}