package com.example.tj.weather.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.tj.weather.R;

/**
 * Created by tj on 3/18/2015.
 * Pretty simple fragment dialog that changes a city then notifies its activity of the change.
 */
public class CitySearchDialogFragment extends DialogFragment {
    private EditText cityInput, stateInput;

    //The callback to the activity.
    public interface CityChangeListener {
        void onCityChanged(String city, String countryOrState);
    }

    public CitySearchDialogFragment() {
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    /*
    the view of the dialog.  I could have used an Alertdialog here as well.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.city_search_layout, container, false);

        cityInput = (EditText) view.findViewById(R.id.city_search_input);

        stateInput = (EditText) view.findViewById(R.id.state_search_input);

        Button searchButton = (Button) view.findViewById(R.id.city_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {

            //Call the activity and send the strings for the city and state or country to the activity for processing.
            @Override
            public void onClick(View v) {
                if (!cityInput.getText().toString().isEmpty()) {
                    ((CityChangeListener) getActivity()).onCityChanged(cityInput.getText().toString().toUpperCase(),
                            stateInput.getText().toString().toUpperCase());
                    clearFields();
                    dismiss();
                }
            }
        });

        Button cancelButton = (Button) view.findViewById(R.id.city_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearFields();
                dismiss();
            }
        });

        return view;
    }

    //For clearing fields between searches.
    private void clearFields() {
        cityInput.setText("");
        stateInput.setText("");
    }
}
