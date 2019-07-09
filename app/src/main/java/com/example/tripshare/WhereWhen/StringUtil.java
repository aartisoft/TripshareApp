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

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.common.base.Splitter;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.List;


/**
 * Utility class for converting objects to viewable strings and back.
 */
final class StringUtil {

  private static final String RESULT_SEPARATOR = "\n---\n\t";

  static void prepend(TextView textView, String prefix) {
    textView.setText(prefix + "\n\n" + textView.getText());
  }

  @Nullable
  static LatLng convertToLatLng(@Nullable String value) {
    if (TextUtils.isEmpty(value)) {
      return null;
    }

    List<String> split = Splitter.on(',').splitToList(value);
    if (split.size() != 2) {
      return null;
    }

    try {
      return new LatLng(Double.parseDouble(split.get(0)), Double.parseDouble(split.get(1)));
    } catch (NullPointerException | NumberFormatException e) {
      return null;
    }
  }
  static String stringify(Place place) {
    return place.getName() + " (" + place.getAddress() + ")";
  }

  static String stringify(Bitmap bitmap) {
    StringBuilder builder = new StringBuilder();

    builder
        .append("Photo size (width x height)")
        .append(RESULT_SEPARATOR)
        .append(bitmap.getWidth())
        .append(", ")
        .append(bitmap.getHeight());

    return builder.toString();
  }

  static String stringifyAutocompleteWidget(Place place) {
    StringBuilder builder = new StringBuilder();

    builder.append("Autocomplete Widget Result:").append(RESULT_SEPARATOR);
    builder.append(stringify(place));
    return builder.toString();
  }
}
