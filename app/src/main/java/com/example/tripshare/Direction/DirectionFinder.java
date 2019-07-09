package com.example.tripshare.Direction;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBdGjUa3OSXHOInYWbLdBCDnS4pHjtGme8";
    private DirectionFinderListener listener;
    private double orlat, orlong,dslat,dslong;
    private static final String TAG = "DirectionFinder";
    private String method;
    private String url;

    public DirectionFinder(DirectionFinderListener listener, double originlat, double originlong, double dslat, double dslong, String way) {
        Log.d(TAG, "DirectionFinder: ords : "+dslat+"\n"+dslong);
        this.listener = listener;
        this.orlat = originlat;
        this.orlong = originlong;
        this.dslat = dslat;
        this.dslong = dslong;
        this.method = way;
    }

    public void execute() throws UnsupportedEncodingException {
        Log.d(TAG, "execute: "+createUrl());
        listener.onDirectionFinderStart();
        //raw한(json형태 그대로인)  json 파일을 다운받는다.
        //어디서? createUrl()에서 받아온 link에서
        //어떻게? 출발,도착지,api키를 가진 url을 통해
        new DownloadRawData().execute(createUrl());
    }
    //throw는 멀까? unsupportedEncodingException은 멀까?
    //인코딩하다 생기는 예외처리를 구현할 예정이라는 거지
    private String createUrl() throws UnsupportedEncodingException {
        //출발, 도착지를 utf-8방식의 인코딩을 한 이후에 url 파라미터 값으로 넣는다.
      /*  String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");*/
        Log.d(TAG, "createUrl: ");

      if (method.equals("driving")){
          //기본값인 자가용으로 검색을 했을 경우
          url = DIRECTION_URL_API + "origin="+orlat+","+orlong+"&destination="+dslat+","+dslong+"&key=" + GOOGLE_API_KEY;
      } else {
          //도보,자전거,공공 등을 사용했을 경우
          url = DIRECTION_URL_API + "origin="+orlat+","+orlong+"&destination="+dslat+","+dslong+"&mode="+method+"&key=" + GOOGLE_API_KEY;
      }
        return url;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //url을 받아온다.
            Log.d(TAG, "doInBackground: params : "+params.toString());
            Log.d(TAG, "doInBackground: params[0] : "+params[0]);
            String link = params[0];
            try {
                URL url = new URL(link);
                Log.d(TAG, "doInBackground: url /"+url);
                //데이터를 해당 url에 접속해 받는다.
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                //받은 데이터에서 한글등 아스키 코드가 아닌 것들이 안깨지게 Reader로 읽는다.
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    //json으로 된 데이터를 한 줄씩 데이터로 만들어
                    //문자열에 한줄 씩 추가한다.
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                //url이 잘못 전달 된경우
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;
        //길찾은 내용이 없으면 바로 끝냄
        Log.d(TAG, "parseJSon: Data "+data);
        //서버에게 받은 길찾는 내용에 대한 문자열을
        //json으로 만든다.
        List<Route> routes = new ArrayList<>();
        //json 객체로 만들고
        JSONObject jsonData = new JSONObject(data);
        //route배열 객체를 가져온다.
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");

        String result = jsonData.getString("status");
        Log.d(TAG, "parseJSon: jsonData.getString(\"status\") "+jsonData.getString("status"));
        Log.d(TAG, "parseJSon:jsonData.get(\"status\") "+jsonData.get("status"));

        if (result.equals("OK")){
            //경로 결과가 있는 경우
            //길에 대한 배열의 크기만큼 가져온다.
            for (int i = 0; i < jsonRoutes.length(); i++) {
                Log.d(TAG, "parseJSon: i "+i);
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                Route route = new Route();

                JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                Log.d(TAG, "parseJSon: "+overview_polylineJson);
                JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                Log.d(TAG, "parseJSon: jsonLegs / "+jsonLegs);
                Log.d(TAG, "parseJSon: "+jsonRoute.getJSONArray("legs"));
                JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                Log.d(TAG, "parseJSon: jsonLegs.getJSONObject(0) "+ jsonLegs.getJSONObject(0));
                JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                Log.d(TAG, "parseJSon:jsonLeg.getJSONObject(\"distance\") "+jsonLeg.getJSONObject("distance"));
                JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                Log.d(TAG, "parseJSon:jsonLeg.getJSONObject(\"duration\") "+jsonLeg.getJSONObject("duration"));
                JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                Log.d(TAG, "parseJSon:jsonLeg.getJSONObject(\"end_location\") "+jsonLeg.getJSONObject("end_location"));
                JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");
                Log.d(TAG, "parseJSon:jsonLeg.getJSONObject(\"start_location\") "+jsonLeg.getJSONObject("start_location"));

                route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
                route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
                route.endAddress = jsonLeg.getString("end_address");
                route.startAddress = jsonLeg.getString("start_address");
                route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                //폴리라인은 인코딩 되있는 데이터가 오므로 디코딩 해줘야함
                route.points = decodePolyLine(overview_polylineJson.getString("points"));

                routes.add(route);
            }
            listener.onDirectionFinderSuccess(routes);
        }else{
            listener.onDirectionFinderFail();
        }

    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
