package kr.co.tacademy.mongsil.mongsil;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseDataParseHandler {
    public static PostData getJSONPostRequestAllList(
            StringBuilder buf) {

        // 전체 - msg 생략
        JSONObject jsonObject = null;

        // post
        JSONArray jsonArray = null;
        PostData jsonPostData;
        ArrayList<Post> jsonPostList = null;

        try {
            jsonObject = new JSONObject(buf.toString());

            jsonArray = jsonObject.getJSONArray("post");
            jsonPostList = new ArrayList<Post>();
            int jsonArrSize = jsonArray.length();
            for (int i = 0; i < jsonArrSize; i++) {
                Post post = new Post();
                JSONObject jData = jsonArray.getJSONObject(i);

                post.postId = jData.getInt("postId");
                post.content = jData.getString("content");
                post.userId = jData.getInt("userId");
                post.username = jData.getString("username");
                post.profileImg = jData.getString("profileImg");
                post.date = jData.getString("date");
                post.area1 = jData.getString("area1");
                post.area2 = jData.getString("area2");

                jsonPostList.add(post);
            }

            jsonPostData = new PostData(jsonPostList);
            jsonPostData.totalCount = jsonObject.getInt("totalCount");

            return jsonPostData;
        } catch (JSONException je) {
            Log.e("GET:PostRequestAllList", "JSON파싱 중 에러발생", je);
        }
        return null;
    }
    public static SearchPoiInfo getJSONPoiList(
            StringBuilder buf) {

        // 전체
        JSONObject jsonObject = null;

        // searchPoiInfo
        JSONObject jsonSearchPoiInfo = null;
        JSONObject jsonPois = null;
        JSONArray jsonArray = null;
        SearchPoiInfo searchPoiInfo;
        ArrayList<Poi> jsonPoiList = null;

        try {
            jsonObject = new JSONObject(buf.toString());
            jsonSearchPoiInfo = jsonObject.getJSONObject("searchPoiInfo");

            jsonPois = jsonSearchPoiInfo.getJSONObject("pois");
            jsonArray = jsonPois.getJSONArray("poi");

            jsonPoiList = new ArrayList<Poi>();
            int jsonArrSize = jsonArray.length();
            for (int i = 0; i < jsonArrSize; i++) {
                Poi poi = new Poi();
                JSONObject jData = jsonArray.getJSONObject(i);

                poi.name = jData.getString("name");
                poi.noorLat = jData.getString("noorLat");
                poi.noorLon = jData.getString("noorLon");
                poi.upperAddrName = jData.getString("upperAddrName");
                poi.middleAddrName = jData.getString("middleAddrName");
                poi.lowerAddrName = jData.getString("lowerAddrName");

                jsonPoiList.add(poi);
            }

            searchPoiInfo = new SearchPoiInfo(jsonPoiList);
            searchPoiInfo.totalCount = jsonSearchPoiInfo.getInt("totalCount");
            searchPoiInfo.page = jsonSearchPoiInfo.getInt("page");

            return searchPoiInfo;
        } catch (JSONException je) {
            Log.e("GET:PoiAllList", "JSON파싱 중 에러발생", je);
        }
        return null;
    }
}
