package kr.co.tacademy.mongsil.mongsil.Constants;

/**
 * Created by Han on 2017-02-20.
 */

public class Constant {

    /*
    * 서버 관련 상수들
     */
    public static final String SK_APP_KEY = "b97acc1a-399a-3ce5-b55a-f6bb4bf021f9";
    public static final String HOST_URL = "52.78.101.201";
    public static final String PORT_NUMBER = ":3003";

    // SK GET Request
    public static final String SK_POI_SEARCH
            = "https://apis.skplanetx.com/tmap/pois?searchKeyword=%s&resCoordType=WGS84GEO&version=1";
    public static final String SK_REVERSE_GEOCOING
            = "https://apis.skplanetx.com/tmap/geo/reversegeocoding?" +
            "lat=" + "%s" + "&lon=" + "%s" + "&coordType=WGS84GEO&version=1";
    public static final String SK_WEATHER_LAT_LON
            = "http://apis.skplanetx.com/weather/current/"
            + "minutely?version=1&lat=%s&lon=%s";
    /*public static final String SK_WEATHER_DONG
            = "http://apis.skplanetx.com/weather/current/"
            + "minutely?version=1&city=%s&county=%s&village=%s";*/

    // SOCKET
    public static final String SOCKET_SERVER_POST
            = "http://" + HOST_URL + PORT_NUMBER;

    // GET
    public static final String GET_SERVER_POST
            = "http://" + HOST_URL + PORT_NUMBER + "/post?area1=%s&skip=%s";
    public static final String GET_SERVER_POST_DETAIL
            = "http://" + HOST_URL + PORT_NUMBER + "/post/%s";
    public static final String GET_SERVER_POST_DETAIL_REPLY
            = "http://" + HOST_URL + PORT_NUMBER + "/post/%s" + "/reply?skip=%s";
    public static final String GET_SERVER_USER_POST
            = "http://" + HOST_URL + PORT_NUMBER + "/users/%s/post?skip=%s";
    public static final String GET_SERVER_USER_REPLY
            = "http://" + HOST_URL + PORT_NUMBER + "/users/%s" + "/reply?skip=%s";
    public static final String GET_SERVER_USER_INFO
            = "http://" + HOST_URL + PORT_NUMBER + "/users/%s";

    // POST
    public static final String POST_SERVER_USER_LOGIN
            = "http://" + HOST_URL + PORT_NUMBER + "/users/login";
    public static final String POST_SERVER_USER_SIGN_UP
            = "http://" + HOST_URL + PORT_NUMBER + "/users";
    public static final String POST_SERVER_POST
            = "http://" + HOST_URL + PORT_NUMBER + "/post";
    public static final String POST_SERVER_POST_REPLY
            = "http://" + HOST_URL + PORT_NUMBER + "/post/%s/reply";

    // PUT
    public static final String PUT_SERVER_POST
            = "http://" + HOST_URL + PORT_NUMBER + "/post/%s";
    public static final String PUT_SERVER_REPLY
            = "http://" + HOST_URL + PORT_NUMBER + "/post/%s/reply/%s";
    public static final String PUT_SERVER_USER_EDIT
            = "http://" + HOST_URL + PORT_NUMBER + "/users/%s";

    // DELETE
    public static final String DELETE_SERVER_POST_REMOVE
            = "http://" + HOST_URL + PORT_NUMBER + "/post/%s";
    public static final String DELETE_SERVER_REPLY_REMOVE
            = "http://" + HOST_URL + PORT_NUMBER + "/post/%s/reply/%s";
    public static final String DELETE_SERVER_USER_REMOVE
            = "http://" + HOST_URL + PORT_NUMBER + "/users/%s";
}
