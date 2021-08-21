package com.learning.texnar13.teachersprogect.sponsor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.learning.texnar13.teachersprogect.R;

// фрагмент последнего слайда
public class SponsorFragment extends Fragment {

    View root;

    // ссылка на загруженные днные о товарах
    LoadedPrice[] loadedPriceList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // раздуваем разметку
        root = inflater.inflate(R.layout.sponsor_activity_screen_final, container);
        // выводим разметку по данным
        if (loadedPriceList != null) outDataInContainersAndSetClickers(root);
        return root;
    }

    public void setPriceData(LoadedPrice[] loadedPriceList) {
        this.loadedPriceList = loadedPriceList;
        // выводим разметку после получения
        if (root != null) outDataInContainersAndSetClickers(root);
    }

    void outDataInContainersAndSetClickers(View root) {
        // контейнер для элементов
        LinearLayout container = root.findViewById(R.id.sponsor_activity_screen_final_content);
        container.removeAllViews();

        // выводим элементы
        for (int priceItemI = 0; priceItemI < loadedPriceList.length; priceItemI++) {
            // вывод разметки кнопки
            View button;
            if (loadedPriceList[priceItemI].trialPeriodDays == 0) {// это подписка без trial
                button = getLayoutInflater().inflate(R.layout.sponsor_activity_button_no_trial, null);
            } else {// с trial периодом
                button = getLayoutInflater().inflate(R.layout.sponsor_activity_button_with_trial, null);
                // выставляем trial текст
                ((TextView) button.findViewById(R.id.sponsor_activity_screen_final_button_trial)).setText(
                        getResources().getString(R.string.sponsor_activity_text_free, loadedPriceList[priceItemI].trialPeriodDays)
                );
            }
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            // если кнопка не последняя выводим отступ снизу
            if (priceItemI != loadedPriceList.length - 1)
                buttonParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.double_margin);
            container.addView(button, buttonParams);
            // выставляем основной текст
            ((TextView) button.findViewById(R.id.sponsor_activity_screen_final_button_title)).setText(getResources().getString(
                    (loadedPriceList[priceItemI].subscriptionPeriodType == LoadedPrice.SUBSCRIPTION_PERIOD_MONTH)
                            ? (R.string.sponsor_activity_button_sub_month)
                            : (R.string.sponsor_activity_button_sub_year),
                    loadedPriceList[priceItemI].price
            ));

            // нажатие на кнопку
            int finalI = priceItemI;
            button.findViewById(R.id.sponsor_activity_screen_final_button).setOnClickListener(
                    v1 -> ((SubsClickInterface) getActivity()).click(finalI));
        }
    }
}

interface SubsClickInterface {
    void click(int buttonNumber);
}

//<string name="sponsor_activity_title_screen_0">##Неограниченное количество уроков</string>
//<string name="sponsor_activity_title_screen_1">##Никаких ограничений по символам (комментарий к уроку и к ученику любой длинны)</string>
//<string name="sponsor_activity_title_screen_2">##Нет рекламы</string>
//<string name="sponsor_activity_title_screen_3">##Сколько угодно типов пропусков и типов работы на уроке</string>
//<string name="sponsor_activity_title_screen_4">##Импорт и экспорт данных</string>
//<string name="sponsor_activity_title_screen_last">##Выберите подходящую для вас подписку:</string>
//<string name="sponsor_activity_button_sub_month" formatted="true">##1 раз в месяц = %s</string>
//<string name="sponsor_activity_button_sub_year" formatted="true">##1 раз в год = %s</string>
//<string name="sponsor_activity_title_cancel_sub">##Вы можете отменить подписку в любое время</string>
//<string name="sponsor_activity_button_my_subs">##Посмотреть мои подписки</string>
//<string name="sponsor_activity_text_test_dz">##Тестовое дз для надписи, напиши здесь что-нибудь:)</string>
//<string name="sponsor_activity_text_test_info">##Тестовая информация об уроке, напиши здесь что-нибудь</string>