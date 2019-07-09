package com.example.tripshare.LoginRegister;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;

/*
ISessionConfig와
IApplicationConfig를 설정할 것이다.
I세션은 로그인 객체를 유지시킴 =Access Token 관리
I앱은 앱이 가지고 있는 정보를 전달함

*/

public class KaKaoSDKAdapter extends KakaoAdapter {
    /**
     * Session Config에 대해서는 default값들이 존재한다.
     * 필요한 상황에서만 override해서 사용하면 됨.
     * @return Session의 설정값.
     */
    @Override
    public ISessionConfig getSessionConfig() {
        return new ISessionConfig() {
            @Override
            public AuthType[] getAuthTypes() {
                return new AuthType[] {AuthType.KAKAO_ACCOUNT};
            }

            @Override
            public boolean isUsingWebviewTimer() {
                return false;
            }

            @Override
            public boolean isSecureMode() {
                return false;
            }

            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }

            @Override
            public boolean isSaveFormData() {
                return true;
            }
        };
    }

    @Override
    public IApplicationConfig getApplicationConfig() {
        return () -> GlobalApplication.getGlobalApplicationContext();
    }
}
