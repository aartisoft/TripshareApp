/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.tripshare.WhereWhen;

import com.example.tripshare.R;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


/**
 * Activity for testing {@link PlacesClient#fetchPlace(FetchPlaceRequest)}.
 */
public class PlaceAndPhotoTestActivity extends AppCompatActivity {

  private PlacesClient placesClient;
  private ImageView photoView;
  private TextView responseView;
  private FieldSelector fieldSelector;
  private static final String TAG = "PlaceAndPhotoTestActivi";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Retrieve a PlacesClient (previously initialized - see StreamingActivity)
    placesClient = Places.createClient(this);

    fieldSelector = new FieldSelector();

            fetchPlace();

    // UI initialization
    setLoading(false);
  }

  private void fetchPlace() {


    //사진 가져올 건지
    final boolean isFetchPhotoChecked = true;
    //장소의 어떤 부분을 가져올 건지 field
    List<Place.Field> placeFields = getPlaceFields();

    if (!validateInputs(isFetchPhotoChecked, placeFields)) {
      return;
      //사진을 없다면 밑에 있는 코드는 종료
    }
    //사진을 가져올 경우에만 이 밑에 코드 실행해서
    //로딩시작
    setLoading(true);

    //장소 아이디와 가져올 field list를 통해 장소에 대한 정보를 요청함
    FetchPlaceRequest request = FetchPlaceRequest.newInstance(getPlaceId(), placeFields);
    Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(request);

    //가져오기에 성공했을 때
    placeTask.addOnSuccessListener(
        (response) -> {
          //텍스트 뷰에
          Log.d(TAG, "fetchPlace: "+response.getPlace().getName());
          Log.d(TAG, "fetchPlace: "+response.getPlace().getAddress());
          Log.d(TAG, "fetchPlace: "+response.getPlace().getLatLng().longitude);
          Log.d(TAG, "fetchPlace: "+response.getPlace().getPhotoMetadatas().get(0));

        });

    placeTask.addOnFailureListener(
        (exception) -> {
          exception.printStackTrace();
          responseView.setText(exception.getMessage());
        });

    placeTask.addOnCompleteListener(response -> setLoading(false));
  }




  private boolean validateInputs(
      boolean isFetchPhotoChecked, List<Field> placeFields) {
    if (isFetchPhotoChecked) {
      //사진을 가져온다.
      if (!placeFields.contains(Field.PHOTO_METADATAS)) { //가져올 사진이 없을 때
        responseView.setText(
            "'Also fetch photo?' is selected, but PHOTO_METADATAS OnedayPlace Field is not.");
        return false;
      }
    }

    return true;
  }

  private String getPlaceId() {
    return "id";
  }
  //가져올 파일들
  private List<Place.Field> getPlaceFields() {
          return fieldSelector.getSelectedFields();
      }

  private void setLoading(boolean loading) {
    findViewById(R.id.loading).setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
  }
}
