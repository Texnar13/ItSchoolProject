package com.learning.texnar13.teachersprogect.sponsor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.material.tabs.TabLayoutMediator;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;

import java.util.ArrayList;
import java.util.List;

public class SponsorActivity extends AppCompatActivity implements SubsClickInterface {

    // количество страниц в превью
    public static final int PAGES_COUNT = 6;
    // кнопка перехода на последний экран
    TextView buttonGoToFinal;

    // текущая ссылка последнего фрагмента
    SponsorFragment currentLastFragment;
    // ссылка на загруженные днные о товарах
    LoadedPrice[] loadedPriceList;

    public static final int RESULT_DEAL_DONE = 5555;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sponsor_activity);

        // создаем разметку для перелистывателя страниц
        ViewPager2 viewPager = findViewById(R.id.sponsor_activity_viewpager);
        FragmentStateAdapter adapter = new androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return PAGES_COUNT;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // обновляем ссылку на текущий фрагмент
                if (position == PAGES_COUNT - 1) {
                    currentLastFragment = new SponsorFragment();
                    // и наполняем его данными если они подгрузились
                    if (loadedPriceList != null) currentLastFragment.setPriceData(loadedPriceList);
                    return currentLastFragment;
                } else
                    return SponsorPreviewFragment.newInstance(position);
            }
        };
        viewPager.setAdapter(adapter);
        // привязываем TabLayout к viewPager
        new TabLayoutMediator(findViewById(R.id.sponsor_activity_tab), viewPager,
                (tab, position) -> {/* тут можно табам текст поставить :)*/ }).attach();

        // кнопка перехода к последнему фрагменту
        buttonGoToFinal = findViewById(R.id.sponsor_activity_go_to_last);
        buttonGoToFinal.setOnClickListener(
                (v) -> viewPager.setCurrentItem(PAGES_COUNT - 1));
        // скрываем ее на последнем экране
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == PAGES_COUNT - 1) {
                    buttonGoToFinal.setTextColor(getResources().getColor(R.color.transparent));
                    buttonGoToFinal.setBackgroundColor(getResources().getColor(R.color.transparent));
                } else {
                    buttonGoToFinal.setTextColor(getResources().getColor(R.color.backgroundWhite));
                    buttonGoToFinal.setBackground(getResources().getDrawable(R.drawable.sponsor_activity_background_button_round_gold));
                }
                super.onPageSelected(position);
            }
        });


        // ---- настраиваем клиент связи с googlePlay ----
        reconnectAttemptsCount = 0;

        // billingClient.queryPurchasesAsync();
        billingClient = BillingClient.newBuilder(this)
                .setListener((billingResult, purchases) -> {
                    // обратная связь диалога покупки google play
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        // обрабатываем покупки, если они есть (предоставляем контент пользователю)
                        if (purchases.size() != 0) {
                            // проверяем что произошло именно событие покупки (а не напримекр промежуточный этап с оплато позже PENDING)
                            if (purchases.get(0).getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                // сохраняем параметр в SharedPreferences
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                        .putBoolean(SharedPrefsContract.PREFS_BOOLEAN_PREMIUM_STATE, true).apply();
                                // при закрытии активности покажем в предыдущей диалог
                                setResult(RESULT_DEAL_DONE);
                                // закрываем активность
                                finish();

                                // подтверждаем что выдали пользователю контент
                                if (!purchases.get(0).isAcknowledged()) {
                                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                                            .newBuilder().setPurchaseToken(purchases.get(0).getPurchaseToken()).build();
                                    // подтверждение на подтверждение
                                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult12 -> {
                                        if (billingResult12.getResponseCode() != BillingClient.BillingResponseCode.OK)
                                            Toast.makeText(this, R.string.sponsor_activity_text_error_to_acknowledge, Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                        }
                    }
//            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//                // пользователь отменил покупку
//                Log.e("TAG", "Handle an error caused by a user cancelling the purchase flow");
//            } else {
//                // Handle any other error codes.
//            }
                }).enablePendingPurchases().build();

        // пытаемся соединиться с сервером googlePlay
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                // BillingClient готов начинаем загрузку каталога
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // id товаров
                    List<String> skuList = new ArrayList<>();
                    skuList.add(getResources().getString(R.string.subscription_id_month_sponsor));
                    skuList.add(getResources().getString(R.string.subscription_id_year_sponsor));

                    // получаем информацию о доступных товарах
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
                    billingClient.querySkuDetailsAsync(params.build(), (billingResult1, skuDetailsList) -> {

                        try {// todo убрать try catch после разработки
                            // сохарняем данные на случай если фрамент перезапустится или еще не создан
                            LoadedPrice[] newList = new LoadedPrice[skuDetailsList.size()];
                            for (int detailsI = 0; detailsI < skuDetailsList.size(); detailsI++) {
                                // получаем данные из конкретного объекта
                                SkuDetails skuDetails = skuDetailsList.get(detailsI);
                                newList[detailsI] = new LoadedPrice(
                                        skuDetails,
                                        parseDaysCount(skuDetails.getFreeTrialPeriod()), // количество бесплатных дней
                                        getSubsPeriodTypeByCode(skuDetails.getSubscriptionPeriod()),
                                        skuDetails.getPrice(),
                                        skuDetails.getDescription()
                                );
                            }
                            // применяем изменения
                            loadedPriceList = newList;

                            // также если фрагмент уже был создан, выводим внего данные через хендлер
                            myHandler.sendEmptyMessage(123);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // нет связи с Google Play
                reconnectAttemptsCount++;
                if (reconnectAttemptsCount < 5) {
                    // пытаемся еще раз
                    billingClient.startConnection(this);
                } else // выводим сообщение об этом
                    myHandler.sendEmptyMessage(222);
            }
        });
    }

    // клиентская часть платежного сервиса // todo при повороте не обновлять оба поля
    private BillingClient billingClient;
    // количество попыток переподключения к платежным сервисам
    private int reconnectAttemptsCount;


    Handler myHandler = new Handler() {// todo handler
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 123) {
                // также если фрагмент уже был создан, выводим внего данные о ценах
                if (currentLastFragment != null) {
                    currentLastFragment.setPriceData(loadedPriceList);
                }
            } else if (msg.what == 222) {
                // ошибка соединения с сервером
                Toast.makeText(SponsorActivity.this, R.string.sponsor_activity_text_error_connection, Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };


    // обратная связь последнего фрагмента, нажатие на кнопки покупок
    @Override
    public void click(int buttonNumber) {
        // запускаем диалог покупок
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(this.loadedPriceList[buttonNumber].skuDetailsObject)
                .build();
        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
        // получаем код результата запуска диалога (не покупки)
        if (responseCode != BillingClient.BillingResponseCode.OK)
            Toast.makeText(
                    this,
                    getResources().getString(R.string.sponsor_activity_text_error_send_purchase_request, responseCode),
                    Toast.LENGTH_SHORT
            ).show();
    }

    // --------- вспомогательные методы ---------

    // получить количество дней из шифра
    int parseDaysCount(String freeTrialPeriod) {
        if (freeTrialPeriod.length() == 0) return 0;

        int answer = 0;
        int tempNumber = 0;
        int tempOrder = 1;
        for (int checkedLengthI = 1; checkedLengthI < freeTrialPeriod.length(); checkedLengthI++) {// remove P, start from 1
            // достаем символ
            char a = freeTrialPeriod.charAt(checkedLengthI);
            if (a == '0' || a == '1' || a == '2' || a == '3' || a == '4' || a == '5' || a == '6' || a == '7' || a == '8' || a == '9') {
                tempNumber = tempNumber * tempOrder + (a - '0'); // char to int
                tempOrder = tempOrder * 10;
            } else {
                if (a == 'w' || a == 'W') {
                    answer += (tempNumber * 7);
                } else if (a == 'd' || a == 'D') {
                    answer += (tempNumber);
                }
                tempNumber = 0;
                tempOrder = 1;
            }
        }
        return answer;
    }

    // тип периода оплаты подписки
    int getSubsPeriodTypeByCode(String code) {
        switch (code) {//P1M
            case "P1Y":
                return LoadedPrice.SUBSCRIPTION_PERIOD_YEAR;
            case "P1M":
                return LoadedPrice.SUBSCRIPTION_PERIOD_MONTH;
            default:
                return -1;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("TAG", "onDestroy");
        // чистим данные
        billingClient.endConnection();
        billingClient = null; // одновременно должно быть только одно соединение, по этому завершаем на всякий случай
        reconnectAttemptsCount = 0;
    }
}


class LoadedPrice {
    public static final int SUBSCRIPTION_PERIOD_YEAR = 0;
    public static final int SUBSCRIPTION_PERIOD_MONTH = 1;

    SkuDetails skuDetailsObject;
    int trialPeriodDays; //P4W2D
    int subscriptionPeriodType; // 0 - year 1 - month // P1M
    String price;
    String description;

    public LoadedPrice(SkuDetails skuDetailsObject, int trialPeriodDays, int subscriptionPeriodType, String price, String description) {
        this.skuDetailsObject = skuDetailsObject;
        this.trialPeriodDays = trialPeriodDays;
        this.subscriptionPeriodType = subscriptionPeriodType;
        this.price = price;
        this.description = description;
    }
}


// подарочные коды
//                с передачей числа
//                try {
//                    String url = "https://play.google.com/redeem?code=" + URLEncoder.encode(
//                            "request code with dialog", "UTF-8");
//                    this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//                } catch (ActivityNotFoundException | UnsupportedEncodingException e) {
//                    // Play Store app is not installed
//                }


//        просто открытие диалога
//        try {
//              startActivity(new Intent(
//              Intent.ACTION_VIEW,
//              Uri.parse("https://play.google.com/redeem?code=")
//              ));
//        } catch (android.content.ActivityNotFoundException e) {
//              // Play Store app is not installed
//        }


//                            a.append(skuDetailsList.get(detailsI).getTitle())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getType())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getFreeTrialPeriod())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getSubscriptionPeriod())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getPriceAmountMicros())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getPrice())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getOriginalPriceAmountMicros())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getOriginalPrice())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getDescription())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getIntroductoryPrice())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getIntroductoryPriceCycles())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getIntroductoryPricePeriod())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getIntroductoryPriceAmountMicros())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getIconUrl())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getSku())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getPriceCurrencyCode())
//                                    .append(" - ").append(skuDetailsList.get(detailsI).getOriginalJson())
//                                    .append('\n');

//#Ежемесячная подписка (Teacher's assistant (grade book))
// subs
// P4W2D      .getFreeTrialPeriod()(30 дней)
// P1M        .getSubscriptionPeriod()
// 50000000 //.getPriceAmountMicros()
// 50,00 ₽ .getPrice()
// 50000000 //.getOriginalPriceAmountMicros
// 50,00 ₽ .getOriginalPrice())
// #Тестовое описание спонсорской подписки .getDescription()
//            .getIntroductoryPrice()
// 0          .getIntroductoryPriceCycles()
//            .getIntroductoryPricePeriod()
// 0          .getIntroductoryPriceAmountMicros()
//            .getIconUrl()
// com.anavana.teachersproject.month_sponsor
// RUB        .getPriceCurrencyCode()
// {..., "skuDetailsToken":"AEuhp4KN8lO-P0g2kyPLNXFpom_MxYdgymdng0xLU3TsnoDSo7R0jxQ1jHlUc6ocPyzz"}.getOriginalJson()
//


// Пользователю также отправляется по электронной почте квитанция о транзакции, содержащая идентификатор заказа или уникальный идентификатор транзакции.
// Вы можете использовать идентификатор заказа для управления возвратом средств в консоли Google Play.