package com.learning.texnar13.teachersprogect.sponsor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SponsorMySubscriptionActivity extends AppCompatActivity {

    // текстовое поле для вывода названия подписки
    private TextView subsNameText;
    // хэндлер для обновления тектового поля после загрузки информации
    static Handler handler;

    // идентификаторы подписок
    private String[] skuIds;


    // загруженные из google данные

    // статус подписки (-1, 0, 1)
    private int subscriptionStatus = -1;
    // стоимость подписки тексьом
    private String costMonth = "";
    private String costYear = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // цвет статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.premium_background, getTheme()));
            // цвет текста в статус баре
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setContentView(R.layout.sponsor_activity_my_subscription);


        // кнопка назад
        findViewById(R.id.sponsor_activity_my_subscription_close_button).setOnClickListener(v -> onBackPressed());


        // todo Остановился здесь


        // текстовое поле для вывода названия подписки
        subsNameText = findViewById(R.id.sponsor_activity_my_subscription_subs_text);

        // кнопка перейти в гуглплей
        findViewById(R.id.sponsor_activity_my_subscription_go_to_google).setOnClickListener(v -> {
            try {
                if (subscriptionStatus != -1) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "https://play.google.com/store/account/subscriptions?sku=" +
                                    skuIds[subscriptionStatus] +
                                    "&package=" + getPackageName()
                    )));
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "https://play.google.com/store/account/subscriptions"
                    )));
                }
            } catch (android.content.ActivityNotFoundException e) {
                // Play Store app is not installed
            }
        });

        // id подписок
        skuIds = new String[]{
                getResources().getString(R.string.subscription_id_month_sponsor),
                getResources().getString(R.string.subscription_id_year_sponsor)
        };

        handler = new Handler(getMainLooper()) {
            // переменные считывающие подгрузились ли все данные
            boolean isSubscriptionLoaded = false;
            boolean isPricesLoaded = false;

            @Override
            public void handleMessage(@NonNull Message msg) {

                // проверка, чтобы вывести графику должны вывестись оба значения
                switch (msg.what) {
                    case 1100:// состояние подписки подгрузилось
                        isSubscriptionLoaded = true;
                        if (isPricesLoaded)
                            outCurrentSubscriptionStateInView();
                        break;
                    case 1101:// цены подписок подгрузились
                        isPricesLoaded = true;
                        if (isSubscriptionLoaded)
                            outCurrentSubscriptionStateInView();
                        break;
                }
                super.handleMessage(msg);
            }
        };


        // асинхронная проверка состояния подписки и получение subscriptionStatus
        checkSubscriptionStatus();

    }


    // получение активных подписок из google
    void checkSubscriptionStatus() {

        final BillingClient billingClient = BillingClient.newBuilder(this).setListener(
                (billingResult, purchases) -> {
                }).enablePendingPurchases().build();

        // пытаемся подключиться
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                billingClient.endConnection();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult connectResult) {

                // связь установлена
                if (connectResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {


                    // получаем данные о покупках и подписках
                    billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, (queryCheckResult, purchasesList) -> {
                        Log.e("tagTag", "Error billingResult.getResponseCode()="
                                + queryCheckResult.getResponseCode() + " purchasesList=" + purchasesList); //todo удалить
                        // данные получены
                        if (queryCheckResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                            // выводим по полученным или не полученным данным состояние подписки
                            getCurrentSubscriptionStateFromPurchasesList(purchasesList);

                        billingClient.endConnection();
                    });


                    // получаем стоимость всех подписок
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(Arrays.asList(skuIds)).setType(BillingClient.SkuType.SUBS);
                    billingClient.querySkuDetailsAsync(params.build(), (billingResult1, skuDetailsList) -> {
                        // не выдает лог ошибок, нужно ставить try catch

                        if (skuDetailsList != null) {
                            for (int detailsI = 0; detailsI < skuDetailsList.size(); detailsI++) {
                                // получаем данные из конкретного объекта
                                SkuDetails skuDetails = skuDetailsList.get(detailsI);
                                if (skuDetails.getSku().equals(skuIds[0])) {
                                    costMonth = skuDetails.getPrice();
                                } else if (skuDetails.getSku().equals(skuIds[1])) {
                                    costYear = skuDetails.getPrice();
                                }
                            }

                            // сообщение хендлеру, о том что данные подгрузились
                            handler.sendEmptyMessage(1101);
                        }
                    });


                } else {
                    billingClient.endConnection();
                }

            }
        });
    }


    // получение из подписок тип текущей подписки (месяц/год), статус и стоимость
    void getCurrentSubscriptionStateFromPurchasesList(List<Purchase> purchasesList) {

        // пробегаемся по всем подпискам которые есть на этом аккаунте, в этом приложении
        for (Purchase purchase : purchasesList) {

            // интересуют только активные подписки
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                // получаем все sku строки данной подписки
                ArrayList<String> tempSkus = purchase.getSkus();

                // и ищем среди этих строк заголовок обозночающий ту или иную подписку
                subscriptionStatus = -1;

                for (String sku : tempSkus) {
                    if (sku.equals(this.skuIds[0])) {// месяц
                        subscriptionStatus = 0;
                        break;
                    } else if (sku.equals(this.skuIds[1])) {// год
                        subscriptionStatus = 1;
                        break;
                    }
                }
            }
        }
        // сообщение хендлеру, о том что данные подгрузились
        handler.sendEmptyMessage(1100);
    }

    // вывод состояние подписки в текстовое поле
    void outCurrentSubscriptionStateInView() {
        switch (subscriptionStatus) {
            case 0:
                subsNameText.setText(getResources().getString(R.string.sponsor_activity_button_sub_month, costMonth));
                break;
            case 1:
                subsNameText.setText(getResources().getString(R.string.sponsor_activity_button_sub_year, costYear));
                break;
            default:
                subsNameText.setText(R.string.sponsor_activity_my_premium_my_sub_connection_error);
        }
    }

}