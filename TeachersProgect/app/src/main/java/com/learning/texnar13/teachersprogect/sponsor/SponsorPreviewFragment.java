package com.learning.texnar13.teachersprogect.sponsor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.learning.texnar13.teachersprogect.R;

public class SponsorPreviewFragment extends Fragment {

    private static final String ARG_SCREEN_POZ = "PozId";

    // здесь для передачи параметров мы используем bundle, который будет сохранен в самом фрагменте
    public static SponsorPreviewFragment newInstance(int screenPoz) {
        SponsorPreviewFragment fragment = new SponsorPreviewFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_SCREEN_POZ, screenPoz);
        fragment.setArguments(args);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            switch (getArguments().getInt(ARG_SCREEN_POZ)) {
                case 1:
                    return inflater.inflate(R.layout.sponsor_activity_screen_1, container);
                case 2:
                    return inflater.inflate(R.layout.sponsor_activity_screen_2, container);
                case 3:
                    return inflater.inflate(R.layout.sponsor_activity_screen_3, container);
                case 4:
                    return inflater.inflate(R.layout.sponsor_activity_screen_4, container);
                default:
                    return inflater.inflate(R.layout.sponsor_activity_screen_0, container);
            }
        } else {
            return inflater.inflate(R.layout.sponsor_activity_screen_0, container);
        }
    }
}