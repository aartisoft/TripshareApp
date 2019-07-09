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

import com.google.android.libraries.places.api.model.Place;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Helper class for selecting {@link Place.Field} values.
 */
final class FieldSelector {

  private  List<PlaceField> placeFields;
  private  TextView outputView;


  public FieldSelector() {

  }


  public FieldSelector(CheckBox enableView, TextView outputView) {
    this(enableView, outputView, Arrays.asList(Place.Field.values()));
  }

  public FieldSelector(CheckBox enableView, TextView outputView, List<Place.Field> validFields) {
    placeFields = new ArrayList<>();
    for (Place.Field field : validFields) {
      placeFields.add(new PlaceField(field));
    }

    outputView.setOnClickListener(
        v -> {
          if (v.isEnabled()) {
          }
        });

    enableView.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          outputView.setEnabled(isChecked);
          if (isChecked) {
          } else {
            outputView.setText("");
            for (PlaceField placeField : placeFields) {
              placeField.checked = false;
            }
          }
        });

    this.outputView = outputView;
  }

  /**
   * Returns all {@link Place.Field} values except those passed in.
   *
   * <p>Convenience method for when most {@link Place.Field} values are desired. Useful for APIs
   * that do no support all {@link Place.Field} values.
   */
  static List<Place.Field> getPlaceFields(Place.Field... placeFieldsToOmit) {
    // Arrays.asList is immutable, create a mutable list to allow removing fields
    List<Place.Field> placeFields = new ArrayList<>(Arrays.asList(Place.Field.values()));
    placeFields.removeAll(Arrays.asList(placeFieldsToOmit));

    return placeFields;
  }



  /**
   * Returns all {@link Place.Field} that are selectable.
   */


  /**
   * Returns all {@link Place.Field} values the user selected.
   */
  public List<Place.Field> getSelectedFields() {
    //장소에 대해서 가져올 정보
    List<Place.Field> selectedList = new ArrayList<>();

    selectedList.add(Place.Field.ADDRESS);
    selectedList.add(Place.Field.LAT_LNG);
    selectedList.add(Place.Field.NAME); selectedList.add(Place.Field.PHOTO_METADATAS);
    selectedList.add(Place.Field.ID);

    return selectedList;
  }

  /**
   * Returns a String representation of all selected {@link Place.Field} values. See {@link
   * #getSelectedFields()}.
   */
  public String getSelectedString() {
    StringBuilder builder = new StringBuilder();
    for (Place.Field field : getSelectedFields()) {
      builder.append(field).append("\n");
    }

    return builder.toString();
  }

  //////////////////////////
  // Helper methods below //
  //////////////////////////

  private static class PlaceField {
    final Place.Field field;
    boolean checked;

    public PlaceField(Place.Field field) {
      this.field = field;
    }
  }

  private static class PlaceFieldArrayAdapter extends ArrayAdapter<PlaceField>
      implements OnItemClickListener {

    public PlaceFieldArrayAdapter(Context context, List<PlaceField> placeFields) {
      super(context, android.R.layout.simple_list_item_multiple_choice, placeFields);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      View view = super.getView(position, convertView, parent);
      PlaceField placeField = getItem(position);
      updateView(view, placeField);

      return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      PlaceField placeField = getItem(position);
      placeField.checked = !placeField.checked;
      updateView(view, placeField);
    }

    private void updateView(View view, PlaceField placeField) {
      if (view instanceof CheckedTextView) {
        CheckedTextView checkedTextView = (CheckedTextView) view;
        checkedTextView.setText(placeField.field.toString());
        checkedTextView.setChecked(placeField.checked);
      }
    }
  }
}
