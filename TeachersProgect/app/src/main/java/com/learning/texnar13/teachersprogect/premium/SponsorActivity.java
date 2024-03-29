package com.learning.texnar13.teachersprogect.premium;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.material.tabs.TabLayoutMediator;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.acceptDialog.AcceptDialog;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;

import java.util.ArrayList;
import java.util.List;

// todo переписать бы тут всё, страшный класс, с устаревшими методами
public class SponsorActivity extends AppCompatActivity implements SubsClickInterface, AcceptDialog.AcceptDialogInterface {

    // количество страниц в превью
    public static final int PAGES_COUNT = 5;
    // кнопка перехода на последний экран
    View buttonGoToFinal;

    // текущая ссылка последнего фрагмента
    SponsorFragment currentLastFragment;
    // ссылка на загруженные днные о товарах
    LoadedPrice[] loadedPriceList;

    public static final int RESULT_DEAL_DONE = 5555;


    // ссылка на помощь с оплатой передаваемая как комментарий рекламного блока
    boolean isLinkKeeperLoaded = false;
    String linkFromKeeper = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // отключаем поворот
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // цвет статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.premium_background, getTheme()));
            // цвет текста в статус баре
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setContentView(R.layout.sponsor_activity);

        // кнопка закрытия активности
        findViewById(R.id.sponsor_activity_close_button).setOnClickListener(v -> finish());


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
                buttonGoToFinal.setAlpha((position == PAGES_COUNT - 1) ? 0 : 1);
                super.onPageSelected(position);
            }
        });


        // ---- настраиваем клиент связи с googlePlay ----
        reconnectAttemptsCount = 0;

        Handler myHandler = new Handler(getMainLooper()) {
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

        // создание клиента соединения с google play
        //  который автоматически подтверждает неподтвержденные покупки

        // billingClient.queryPurchasesAsync();
        billingClient = BillingClient.newBuilder(this).setListener((billingResult, purchases) -> {
            // обратная связь диалога покупки google play
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                // обрабатываем покупки, если они есть (предоставляем контент пользователю)
                if (purchases.size() != 0) {
                    // проверяем что произошло именно событие покупки (а не например промежуточный этап с оплатой позже PENDING)
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
                            // (тк нужно обязательно отпарвить в google уведомление о том, что контент предоставлен пользователю)
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
        // и получить доступные товары
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                // BillingClient готов начинаем загрузку каталога
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // получение информации о подписках из подключенного к google play обьекта billingClient
                    startLoadingSubs(billingClient, myHandler);

                    // получение информации о обычных товарах
                    startLoadingProducts(billingClient);
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


    // получение информации о подписках из подключенного к google play обьекта billingClient
    private void startLoadingSubs(BillingClient billingClient, Handler handlerForOut) {
        // id товаров
        List<String> skuList = new ArrayList<>();
        skuList.add(getResources().getString(R.string.subscription_id_month_sponsor));
        skuList.add(getResources().getString(R.string.subscription_id_year_sponsor));

        // получаем информацию о доступных товарах
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(), (billingResult1, skuDetailsList) -> {
            // не выдает лог ошибок, нужно ставить try catch
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
                            skuDetails.getDescription(),
                            skuDetails.getPriceAmountMicros() / 1000000F,
                            skuDetails.getPriceCurrencyCode()
                    );
                }
                // применяем изменения
                loadedPriceList = newList;

                // также если фрагмент уже был создан, выводим внего данные через хендлер
                handlerForOut.sendEmptyMessage(123);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

//        // подготавливаем запрос из products (включающие id товаров)
//        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
//        products.add(QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(getResources().getString(R.string.subscription_id_month_sponsor))
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build()
//        );
//        products.add(QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(getResources().getString(R.string.subscription_id_year_sponsor))
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build()
//        );
//        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
//                .setProductList(products).build();
//
//        // todo тут одним запросом можно получить и подписки и товары, по этому вот если что проверка
//        // BillingClient.ProductType.SUBS.equals(productDetails.getProductType())
//
//        // получаем информацию о доступных товарах
//        billingClient.queryProductDetailsAsync(params,
//                (BillingResult billingResult1, List<ProductDetails> productDetailsList) -> {
//                    // не выдает лог ошибок, нужно ставить try catch
//                    try {// todo убрать try catch после разработки
//                        // сохарняем данные пока толлько во временный обьект
//                        LoadedPrice[] newList = new LoadedPrice[productDetailsList.size()];
//
//
//                        for (int detailsI = 0; detailsI < productDetailsList.size(); detailsI++) {
//
//
//                            // получаем данные из конкретного объекта
//                            ProductDetails productDetails = productDetailsList.get(detailsI);
//
//
//
//
//                            newList[detailsI] = new LoadedPrice(
//                                    productDetails,
//                                    parseDaysCount(productDetails.getFreeTrialPeriod()), // количество бесплатных дней
//                                    getSubsPeriodTypeByCode(productDetails.getSubscriptionPeriod()),
//                                    productDetails.getPrice(),
//                                    productDetails.getDescription(),
//                                    productDetails.getPriceAmountMicros() / 1000000F,
//                                    productDetails.getPriceCurrencyCode()
//                            );
//                        }
//                        // применяем изменения
//                        loadedPriceList = newList;
//
//                        // также если фрагмент уже был создан, выводим внего данные через хендлер
//                        handlerForOut.sendEmptyMessage(123);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });

    }

    // получение информации о обычных товарах
    private void startLoadingProducts(BillingClient billingClient) {
        // подготавливаем запрос из products (включающие id товаров)
        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        QueryProductDetailsParams.Product p = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(getResources().getString(R.string.premium_pay_link_keeper))
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        products.add(p);
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(products).build();

        billingClient.queryProductDetailsAsync(params,
                (BillingResult billingResult1, List<ProductDetails> productDetailsList) -> {
                    // не выдает лог ошибок, нужно ставить try catch
//                    try {// todo убрать try catch после разработки

                    if (productDetailsList.size() != 0) {
                        ProductDetails pd = productDetailsList.get(0);
                        SponsorActivity.this.linkFromKeeper = pd.getDescription();
                        SponsorActivity.this.isLinkKeeperLoaded = true;
                    }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                });
    }


    // клиентская часть платежного сервиса // todo при повороте не обновлять оба поля
    private BillingClient billingClient;
    // количество попыток переподключения к платежным сервисам
    private int reconnectAttemptsCount;


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

    // обратная связь диалога проблемы с подпиской
    @Override
    public void accept() {
        // переход по ссылке
        try {
            // если пользователь с русским языком
            if (getResources().getInteger(R.integer.current_locale_code) == 2) {

                // если какой-то текст был получен
                if (isLinkKeeperLoaded) {
                    String linkText = linkFromKeeper.trim();
                    if (!linkText.equals("")) {
                        // переходим по ссылке
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(linkText)));
                    }
                }
            } else {
                // если пользователь с другим языком

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                // The intent does not have a URI, so declare the "text/plain" MIME type
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"teachersassistant@yandex.ru"}); // recipients
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Payment problem");
                //emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://path/to/email/attachment"));
                // You can also attach multiple items by passing an ArrayList of Uris
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                //todo странно работает, показывает многие другие приложения а не только почту
            }
        } catch (android.content.ActivityNotFoundException e) {
            e.printStackTrace();
        }
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


    static class LoadedPrice {
        public static final int SUBSCRIPTION_PERIOD_YEAR = 0;
        public static final int SUBSCRIPTION_PERIOD_MONTH = 1;

        SkuDetails skuDetailsObject;
        int trialPeriodDays; // пример исходных данных: P4W2D
        int subscriptionPeriodType; // 0 - year 1 - month // пример исходных данных: P1M
        String price;// красиво отформатированная цена
        String description;// описание
        float payValue; // численная стоимость в местной валюте
        String code; // код валюты

        public LoadedPrice(SkuDetails skuDetailsObject, int trialPeriodDays, int subscriptionPeriodType, String price, String description, float payValue, String code) {
            this.skuDetailsObject = skuDetailsObject;
            this.trialPeriodDays = trialPeriodDays;
            this.subscriptionPeriodType = subscriptionPeriodType;
            this.price = price;
            this.description = description;
            this.payValue = payValue;
            this.code = code;
        }
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