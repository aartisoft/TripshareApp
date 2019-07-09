package com.example.tripshare;


import com.example.tripshare.Data.Example;
import com.example.tripshare.Data.Message;
import com.example.tripshare.Data.Messagelist;
import com.example.tripshare.Data.OnedayPlace;
import com.example.tripshare.Data.PlaceList;
import com.example.tripshare.Data.ResultsList;
import com.example.tripshare.Data.Room;
import com.example.tripshare.Data.RoomList;
import com.example.tripshare.Data.TripData;
import com.example.tripshare.Data.TripList;
import com.example.tripshare.Data.UserList;
import com.example.tripshare.LiveStream.Livestream;
import com.example.tripshare.LiveStream.StreamRoomList;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.WhereWhen.Trip;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/*
http통신을 하기 위한 인터페이스
데이터를 전달하는 방식과 어떤 파일에 데이터를 보낼지
보낼 데이터는 어떤 것인지를 보여준다.

retrofit annotation >> request method(요청방식) >>GET
resource(파일)이 명시된다.
resource에 해당하는 파일에 get방식으로

해당 데이터를 보낸다.

*/

public interface ApiInterface {
    //회원가입을 위한 요청
    @FormUrlEncoded
    @POST("register.php")
    Call<User> performRegistration(@Field("email") String Email,
                                   @Field("name") String Name,
                                   @Field("password") String password,
                                   @Field("image") String image);

    //카톡, 페북 로그인
    @GET("snslogin.php")
    Call<User> performSNSLogin(@Query("email") String email,
                               @Query("name") String name,
                               @Query("password") String password,
                               @Query("image") String image,
                               @Query("token") String token);

    //기본이미지 회원가입
    @GET("deimgregister.php")
    Call<User> performDeImgregister(@Query("email") String email,
                                    @Query("name") String name,
                                    @Query("password") String password,
                                    @Query("image") String image);

    @GET("snslogout.php")
    Call<User> performlogout(@Query("email") String email);

    //로그인을 위한 요청
    @FormUrlEncoded
    @POST("login.php")
    Call<User> performUserLogin(@Field("email") String Email,
                                @Field("password") String Password,
                                @Field("token") String token);

    //메인화면에서 프사,이름를 받기위한 요청
    @FormUrlEncoded
    @POST("getimage.php")
    Call<User> Givememyimage(@Field("email") String Email);

    //프사랑 이름 수정할래요~~
    @FormUrlEncoded
    @POST("editprofile.php")
    Call<User> Editmyimage(@Field("email") String Email,
                           @Field("name") String name,
                           @Field("image") String image,
                           @Field("status") String status);

    //여행 일정을 저장할거야
    @FormUrlEncoded
    @POST("tripplan/plandetail.php")
    Call<Trip> Plusmytrip(@Field("placename") String name,
                          @Field("locationid") String locationid,
                          @Field("tstart") String tstart,
                          @Field("tend") String tend,
                          @Field("howlong") Integer howlong,
                          @Field("email") String email,
                          @Field("latitude") double latitude,
                          @Field("longitude") double longitude,
                          @Field("countrycode") String countrycode);

    //메인화면에 내 여행을 가져와줘
    @FormUrlEncoded
    @POST("tripplan/selectplan.php")
    Call<TripList> Givememytrip(@Field("email") String Email);

    //메인화면에 내 여행 일정을 가져와줘
    @FormUrlEncoded
    @POST("tripplan/delete.php")
    Call<Trip> Deletemytrip(@Field("tnum") int tnum);

    @FormUrlEncoded
    @POST("tripplan/editmytrip.php")
    Call<Trip> Editmytrip(@Field("tnum") int tnum,
                          @Field("locationid") String id,
                          @Field("placename") String name,
                          @Field("latitude") double latitude,
                          @Field("longitude") double longitude,
                          @Field("countrycode") String countrycode,
                          @Field("term") int term);

    @FormUrlEncoded
    @POST("tripplan/editmytripterm.php")
    Call<Trip> Editmytripterm(@Field("tnum") int tnum,
                              @Field("tstart") String tstart,
                              @Field("tend") String tend,
                              @Field("howlong") Integer howlong);

    //내 일별 여행 일정을 가져와줘
    @FormUrlEncoded
    @POST("tripplan/selectoneday.php")
    Call<PlaceList> Givemyoneday(@Field("tnum") int tnum,
                                 @Field("date") int date);

    //내 일별 여행일정에 장소 추가
    @FormUrlEncoded
    @POST("tripplan/insertplace.php")
    Call<OnedayPlace> Plusmyoneday(@Field("tnum") int tnum,
                                   @Field("name") String name,
                                   @Field("id") String id,
                                   @Field("latitude") double latitude,
                                   @Field("longitude") double longitude,
                                   @Field("date") int date,
                                   @Field("numorder") int numorder);

    //스와이프로 장소를 삭제했을 때
    @FormUrlEncoded
    @POST("tripplan/deleteoneplace.php")
    Call<OnedayPlace> DeletemyonePlace(@Field("tnum") int tnum,
                                       @Field("date") int date,
                                       @Field("numorder") int numorder,
                                       @Field("updateorder") String update,
                                       @Field("listsize") int size);

    //드래그 앤 드랍으로 장소들 간의 위치를수정했을 때
    @FormUrlEncoded
    @POST("tripplan/editmyplace.php")
    Call<OnedayPlace> Editmyplace(@Field("porderlist") String order,
                                  @Field("placesize") int placesize);

    //로그인을 했을 때 토큰을 저장하는 요청
    @FormUrlEncoded
    @POST("fcm/push_notification.php")
    Call<User> friendnoti(@Field("email") String Email,
                          @Field("fromemail") String fromemail,
                          @Field("name") String name,
                          @Field("tnum") int tnum,
                          @Field("placename") String placename);

    //일정 초대 승락했을 경우 일정이 추가 되는 것.
    @FormUrlEncoded
    @POST("fcm/friendtrip.php")
    Call<User> friendtrip(@Field("tnum") int tnum,
                          @Field("email") String email,
                          @Field("fromemail") String fromemail);

    //초대받은 여행일 경우 tnum을 초대한 tnum으로 변경함
    @FormUrlEncoded
    @POST("fcm/checktnum.php")
    Call<TripData> checkplus(@Field("tnum") int tnum,
                             @Field("locationid") String locationid,
                             @Field("tstart") String tstart,
                             @Field("tend") String tend,
                             @Field("countrycode") String countrycode);

    //나라별 관광지 가져와 주세요!!!
    @FormUrlEncoded
    @POST("tripplan/spot.php")
    Call<ResultsList> spotlist(@Field("countrycode") String countrycode);

    //선별된 나라별 관광지 가져와 주세요!!!
    @FormUrlEncoded
    @POST("tripplan/otherspot.php")
    Call<ResultsList> selectedspot(@Field("city") String city,
                                   @Field("kind") String kind,
                                   @Field("countrycode") String countrycode);

    //추가로 관광지 더 주세요!!!
    @FormUrlEncoded
    @POST("tripplan/plusspot.php")
    Call<ResultsList> plusspot(@Field("city") String city,
                               @Field("kind") String kind,
                               @Field("countrycode") String countrycode,
                               @Field("numrequest") int numrequest);

    //날씨 정보보줘
    //과거
    @GET("past-weather.ashx")
    Call<Example> weather(@Query("key") String key,
                          @Query("q") String latlng,
                          @Query("format") String json,
                          @Query("date") String start,
                          @Query("enddate") String end);

    //미래 예보
    @GET("weather.ashx")
    Call<Example> postweather(@Query("key") String key,
                              @Query("q") String latlng,
                              @Query("format") String json,
                              @Query("num_of_days") String num);

    //현지 시간 알려주세요
    @GET("tz.ashx")
    Call<Example> tourcitytime(@Query("key") String key,
                               @Query("q") String latlng,
                               @Query("format") String json);

    //번역해주세요
    @GET("v2/")
    Call<Example> translate(@Query("q") String q,
                            @Query("source") String source,
                            @Query("target") String target,
                            @Query("key") String key);

    //지도에서 봤는데 선택한 일정에 추가해주세요!!!
    @FormUrlEncoded
    @POST("tripplan/plusplace.php")
    Call<TripData> plusday(@Field("tnum") int tnum,
                           @Field("latitude") double latitude,
                           @Field("longitude") double longitude,
                           @Field("name") String name,
                           @Field("date") int date);

    //친구 추가할려고 하는데 내가 검색한 친구 email 존재하니?
    @FormUrlEncoded
    @POST("talktrip/checkemail.php")
    Call<User> Searchfriend(@Field("email") String email);

    //친구 email 존재하니까 친구추가 한다 ㅇㅋ?
    @FormUrlEncoded
    @POST("talktrip/plusfriend.php")
    Call<User> plusfriend(@Field("myemail") String email,
                          @Field("youremail") String youremail);

    //추가한 친구 리스트 주세요^^
    @FormUrlEncoded
    @POST("talktrip/myfriends.php")
    Call<UserList> myfriendlist(@Field("myemail") String email);

    //추가한 친구 차단 박아요. 연락하지 마세요. 와도 못받아요^^
    @FormUrlEncoded
    @POST("talktrip/getawayfromme.php")
    Call<User> getawayfromme(@Field("myemail") String email,
                             @Field("youremail") String youremail);

    //메세지 저장, 채팅방 없으면 같이 저장
    @FormUrlEncoded
    @POST("talktrip/messageandroom.php")
    Call<Message> messageandroom(@Field("senderemail") String senderemail,
                                 @Field("receiveremail") String receiveremail,
                                 @Field("message") String message,
                                 @Field("ymd") String ymd,
                                 @Field("hm") String hm);

    //내 채팅방 목록 주세요
    @FormUrlEncoded
    @POST("talktrip/roomlist.php")
    Call<RoomList> myroomlist(@Field("myemail") String email);

    //이 사람과 채팅이 처음이든 아니든 채팅방 번호 줘...
    @FormUrlEncoded
    @POST("talktrip/chatroomnum.php")
    Call<Room> chatroomnum(@Field("myemail") String email,
                           @Field("youremail") String youremail);

    //받은 채팅 번호로 저장된 채팅리스트 줄래?
    @FormUrlEncoded
    @POST("talktrip/messagelist.php")
    Call<Messagelist> messagelist(@Field("roomnum") String roomnum,
                                  @Field("myemail") String myemail,
                                  @Field("myname") String myname);

    //드디어 여러 사용자 채팅방 저장한다!!
    @FormUrlEncoded
    @POST("talktrip/savegroupchat.php")
    Call<Room> savegroupchat(@Field("myemail") String email,
                             @Field("othersemail") String others,
                             @Field("total") String total);

    //다중 채팅방일 경우 메세지만 저장
    @FormUrlEncoded
    @POST("talktrip/multiroommessage.php")
    Call<Message> multiroommessage(@Field("senderemail") String senderemail,
                                   @Field("rnum") String rnum,
                                   @Field("message") String message,
                                   @Field("ymd") String ymd,
                                   @Field("hm") String hm);

    //해당 채팅방 나가기
    @FormUrlEncoded
    @POST("talktrip/getoutroom.php")
    Call<Room> getoutroom(@Field("email") String senderemail,
                          @Field("Rnum") String email,
                          @Field("total") String total,
                          @Field("ymd") String ymd,
                          @Field("hm") String hm);

    //해당 친구 이미지랑 이름 주세요
    @FormUrlEncoded
    @POST("talktrip/getoutroom.php")
    Call<Room> friendsinfo(@Field("email") String senderemail,
                           @Field("Rnum") String email,
                           @Field("total") String total,
                           @Field("ymd") String ymd,
                           @Field("hm") String hm);

    //해당 추가된 친구 이메일을 저장해주세요
    @FormUrlEncoded
    @POST("talktrip/plusedroom.php")
    Call<Room> saveplusedemail(@Field("myemail") String senderemail,
                               @Field("othersemail") String othersemail,
                               @Field("Rnum") String email,
                               @Field("total") String total,
                               @Field("othersname") String othersname,
                               @Field("ymd") String ymd,
                               @Field("hm") String hm,
                               @Field("choosedname") String choosedname);

    //이미지 저장
    @FormUrlEncoded
    @POST("talktrip/imagesave.php")
    Call<Message> imagesave(@Field("senderemail") String senderemail,
                            @Field("rnum") String rnum,
                            @Field("message") String message,
                            @Field("ymd") String ymd,
                            @Field("hm") String hm,
                            @Field("type") String image);

    //채팅방에서 여러 이미지를 갤러리에서 선택해서 보낼 때 서버에 저장하는 것
    @Multipart
    @POST("talktrip/imagesupload.php")
    Call<Messagelist> upload(@Part("senderemail") RequestBody senderemail,
                             @Part("rnum") RequestBody rnum,
                             @Part("ymd") RequestBody ymd,
                             @Part("hm") RequestBody hm,
                             @Part("size") RequestBody size,
                             @Part List<MultipartBody.Part> photo);


    //스트리밍 1.방송 만들고 2.서버에 저장하고 3.방송 시작하는 것.
    @FormUrlEncoded
    @POST("streaming/startstream.php")
    Call<Livestream> startroom(@Field("username") String username,
                               @Field("roomname") String roomname);

    //스트리밍 방송을 내 서버에 저장한다.
    @FormUrlEncoded
    @POST("streaming/savestream.php")
    Call<Livestream> saveroom(@Field("username") String username,
                              @Field("roomname") String roomname,
                              @Field("walletaddress") String walletaddress);

    //저장된 방송을 내 서버에서 가져온다.
    @POST("streaming/streamroomlist.php")
    Call<StreamRoomList> giveroomlist();


    //라이브 방송을 종료하고 삭제한다..
    @FormUrlEncoded
    @POST("streaming/delete.php")
    Call<Livestream> deleteroom(@Field("roomname") String name);

    //이미지 메세지리스트 가져오기..
    @FormUrlEncoded
    @POST("talktrip/imagelist.php")
    Call<Messagelist> imagemessagelist(@Field("rnum") String roomnum);
}



