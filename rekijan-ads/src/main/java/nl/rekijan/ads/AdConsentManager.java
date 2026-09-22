package nl.rekijan.ads;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Wraps the Google User Messaging Platform (UMP) consent flow.
 *
 * EEA/UK users must be asked for consent before any personalized ad request
 * is made. This class handles that check, shows the consent form only when
 * required, and only then initializes the Mobile Ads SDK.
 *
 * One instance per Activity that shows ads (usually your main/host Activity).
 * Call {@link #requestConsentThenInit(Callback, String...)} once, early, before showing
 * any {@link RekijanBannerAdView} or requesting a rewarded ad
 *
 * @author Erik-Jan Krielen rekijan.apps@gmail.com
 * @since 22-9-2026
 */
public class AdConsentManager {

    private static final String TAG = "RekijanAds";

    /** Called once consent has been resolved and the Mobile Ads SDK is ready to use. */
    public interface Callback {
        void onReady();
        /** Non-fatal: consent gathering failed, e.g. no network. Caller may retry or skip ads. */
        void onFailure(@NonNull String reason);
    }

    private final Activity activity;
    private final ConsentInformation consentInformation;

    public AdConsentManager(@NonNull Activity activity) {
        this.activity = activity;
        this.consentInformation = UserMessagingPlatform.getConsentInformation(activity);
    }

    /**
     * @param debugDeviceIds Optional list of test device hashed IDs (logcat prints the ID the
     *                        first time UMP runs on a device). Pass null/empty in release builds.
     */
    public void requestConsentThenInit(@NonNull Callback callback, String... debugDeviceIds) {
        ConsentRequestParameters.Builder paramsBuilder = new ConsentRequestParameters.Builder();

        if (debugDeviceIds != null && debugDeviceIds.length > 0) {
            ConsentDebugSettings.Builder debugBuilder = new ConsentDebugSettings.Builder(activity);
            for (String id : debugDeviceIds) {
                debugBuilder.addTestDeviceHashedId(id);
            }
            paramsBuilder.setConsentDebugSettings(debugBuilder.build());
        }

        ConsentRequestParameters params = paramsBuilder.build();

        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        (FormError loadAndShowError) -> {
                            if (loadAndShowError != null) {
                                Log.w(TAG, "Consent form error: " + loadAndShowError.getMessage());
                            }
                            // Whether or not consent was required/granted, we can now init the SDK.
                            initializeMobileAds(callback);
                        }
                ),
                (FormError requestConsentError) -> {
                    Log.w(TAG, "Consent info update failed: " + requestConsentError.getMessage());
                    callback.onFailure(requestConsentError.getMessage());
                }
        );
    }

    /** True once consent status is known and ads (personalized or not) may be requested. */
    public boolean canRequestAds() {
        return consentInformation.canRequestAds();
    }

    private void initializeMobileAds(@NonNull Callback callback) {
        if (!consentInformation.canRequestAds()) {
            callback.onFailure("Consent not granted; ads unavailable.");
            return;
        }
        MobileAds.initialize(activity, initStatus -> callback.onReady());
    }
}