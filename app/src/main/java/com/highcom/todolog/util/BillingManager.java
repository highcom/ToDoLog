package com.highcom.todolog.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.highcom.todolog.R;

import java.util.Arrays;
import java.util.List;

import static com.highcom.todolog.SettingActivity.PREF_FILE_NAME;

/**
 * Google Play Billing管理クラス
 * @noinspection Since15
 */
public class BillingManager implements PurchasesUpdatedListener {
    private static final String TAG = "BillingManager";

    private BillingClient billingClient;
    private Context context;
    private BillingListener listener;
    private boolean isServiceConnected = false;

    public interface BillingListener {
        void onPurchaseSuccess();
        void onPurchaseFailed(String error);
        void onBillingServiceDisconnected();
    }

    public BillingManager(Context context, BillingListener listener) {
        this.context = context;
        this.listener = listener;
        initializeBillingClient();
    }

    private void initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .enableAutoServiceReconnection()
                .build();

        connectToBillingService();
    }

    private void connectToBillingService() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    isServiceConnected = true;
                    Log.d(TAG, "Billing service connected");
                    checkExistingPurchases();
                } else {
                    isServiceConnected = false;
                    Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                isServiceConnected = false;
                Log.d(TAG, "Billing service disconnected");
                if (listener != null) {
                    listener.onBillingServiceDisconnected();
                }
            }
        });
    }

    /**
     * 既存の購入を確認
     */
    private void checkExistingPurchases() {
        if (!isServiceConnected) return;

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : purchases) {
                            if (purchase.getProducts().contains(BillingConstants.PRODUCT_ID_REMOVE_ADS)) {
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    // 広告削除購入済み
                                    setAdsRemoved(true);
                                    Log.d(TAG, "Ads removal purchase found");
                                }
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     * 広告削除の課金フロー開始
     */
    public void purchaseRemoveAds(Activity activity) {
        if (!isServiceConnected) {
            connectToBillingService();
            return;
        }

        QueryProductDetailsParams queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    Arrays.asList(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(BillingConstants.PRODUCT_ID_REMOVE_ADS)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build();

        billingClient.queryProductDetailsAsync(
            queryProductDetailsParams,
            new ProductDetailsResponseListener() {
                @Override
                public void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                                   @NonNull QueryProductDetailsResult queryProductDetailsResult) {
                    List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && !productDetailsList.isEmpty()) {

                        ProductDetails productDetails = productDetailsList.get(0);
                        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                            Arrays.asList(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .build()
                            );

                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

                        billingClient.launchBillingFlow(activity, billingFlowParams);
                    } else {
                        if (listener != null) {
                            listener.onPurchaseFailed(context.getString(R.string.billing_error_query_product_details));
                        }
                    }
                }
            }
        );
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getProducts().contains(BillingConstants.PRODUCT_ID_REMOVE_ADS)) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        // 購入成功
                        setAdsRemoved(true);
                        if (listener != null) {
                            listener.onPurchaseSuccess();
                        }
                        Log.d(TAG, "Purchase successful");

                        // 消費可能な場合は消費する
                        if (!purchase.isAcknowledged()) {
                            acknowledgePurchase(purchase);
                        }
                    }
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // ユーザーがキャンセル
            Log.d(TAG, "Purchase canceled by user");
        } else {
            // 購入失敗
            if (listener != null) {
                listener.onPurchaseFailed(context.getString(R.string.billing_error_purchase_failed) + ": " + billingResult.getDebugMessage());
            }
            Log.e(TAG, "Purchase failed: " + billingResult.getDebugMessage());
        }
    }

    /**
     * 購入の確認
     */
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams =
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged");
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: " + billingResult.getDebugMessage());
            }
        });
    }

    /**
     * 広告削除状態を取得
     */
    public static boolean isAdsRemoved(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(BillingConstants.PREF_ADS_REMOVED, false);
    }

    /**
     * 広告削除状態を設定
     */
    private void setAdsRemoved(boolean removed) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(BillingConstants.PREF_ADS_REMOVED, removed).apply();
    }

    /**
     * BillingClientの解放
     */
    public void destroy() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
        }
    }
}