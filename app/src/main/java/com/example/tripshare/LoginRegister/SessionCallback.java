package com.example.tripshare.LoginRegister;

import android.util.Log;

import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

public class SessionCallback implements ISessionCallback {


    private static final String TAG = "Session";

    @Override
    public void onSessionOpened() {

        UserManagement.getInstance().requestMe(new MeResponseCallback() {

            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.d(TAG, "onFailure: "+errorResult);
                String message = "failed to get user info. msg=" + errorResult;
                ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                if (result == ErrorCode.CLIENT_ERROR_CODE) {
                    //에러로 인한 로그인 실패
//                        finish();
                } else {
                    //redirectMainActivity();
                }
            }
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d(TAG, "onSessionClosed: "+errorResult);
            }
            @Override
            public void onNotSignedUp() {
                Log.d(TAG, "onNotSignedUp: ");
            }
            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.d(TAG, "onSuccess: ");
                //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.

//                    Log.e("UserProfile", userProfile.toString());
//                    Log.e("UserProfile", userProfile.getId() + "");
                long number = userProfile.getId();
            }
        });

    }
    @Override
    public void onSessionOpenFailed(KakaoException exception) {
        Log.d(TAG, "onSessionOpenFailed: "+exception.toString());
        // 세션 연결이 실패했을때
        // 어쩔때 실패되는지는 테스트를 안해보았음 ㅜㅜ
    }
}
//
//    // 로그인에 성공한 상태
//    @Override
//    public void onSessionOpened() {
//        requestMe();
//        Log.d(TAG, "onSessionOpened: ");
//    }
//
//    // 로그인에 실패한 상태
//    @Override
//    public void onSessionOpenFailed(KakaoException exception) {
//        Log.d(TAG, "onSessionOpenFailed : " + exception.getMessage());
//    }
//
//    // 사용자 정보 요청
//    public void requestMe() {
//        // 사용자정보 요청 결과에 대한 Callback
//        UserManagement.getInstance().requestMe(new MeResponseCallback() {
//            // 세션 오픈 실패. 세션이 삭제된 경우,
//
//            @Override
//            public void onSessionClosed(ErrorResult errorResult) {
//                Log.d(TAG, "onSessionClosed : " + errorResult.getErrorMessage());
//            }
//
//            // 회원이 아닌 경우,
//            @Override
//            public void onNotSignedUp() {
//                Log.d(TAG, "onNotSignedUp");
//            }
//
//            // 사용자정보 요청에 성공한 경우,
//            @Override
//            public void onSuccess(UserProfile userProfile) {
//                Log.d(TAG, "onSuccess");
//                String nickname = userProfile.getNickname();
//                String email = userProfile.getEmail();
//                String profileImagePath = userProfile.getProfileImagePath();
//                String thumnailPath = userProfile.getThumbnailImagePath();
//                String UUID = userProfile.getUUID();
//
//                long id = userProfile.getId();
//                Log.d(TAG,"Profile : "+ nickname + "");
//                Log.d(TAG,"Profile : "+ email + "");
//                Log.d(TAG,"Profile : "+ profileImagePath  + "");
//                Log.d(TAG,"Profile : "+thumnailPath + "");
//                Log.d(TAG,"Profile : "+ UUID + "");
//                Log.d(TAG,"Profile : "+ id + "");
//            }
//
//            // 사용자 정보 요청 실패
//            @Override
//            public void onFailure(ErrorResult errorResult) {
//                Log.d(TAG,"SessionCallback :: "+ "onFailure : " + errorResult.getErrorMessage());
//            }
//        });
//    }
//}
