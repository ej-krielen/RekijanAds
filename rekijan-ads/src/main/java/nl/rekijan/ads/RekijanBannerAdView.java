package nl.rekijan.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

/**
 * Small banner ad bar, meant for docking at the bottom of a screen.
 *
 * Usage (XML):
 * <pre>{@code
 * <nl.rekijan.ads.RekijanBannerAdView
 *     android:id="@+id/bottomAdBar"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:layout_gravity="bottom" />
 * }</pre>
 *
 * Usage (code), after {@link AdConsentManager} reports ready:
 * <pre>{@code
 * bottomAdBar.load("ca-app-pub-xxxx/yyyy");
 * }</pre>
 *
 * The view collapses to zero height until an ad successfully loads, and
 * collapses again if the ad fails to load, so it never leaves a blank gap.
 */
public class RekijanBannerAdView extends FrameLayout {

    private static final String TAG = "RekijanAds";

    @Nullable
    private AdView adView;

    public RekijanBannerAdView(@NonNull Context context) {
        super(context);
    }

    public RekijanBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RekijanBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Loads and shows a banner using the given ad unit ID. Safe to call again later
     * (e.g. with a different unit ID) — the previous AdView is torn down first.
     */
    public void load(@NonNull String adUnitId) {
        destroyCurrentAd();

        AdView newAdView = new AdView(getContext());
        newAdView.setAdUnitId(adUnitId);
        newAdView.setAdSize(AdSize.BANNER);
        newAdView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                setVisibility(VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                Log.w(TAG, "Banner failed to load: " + adError.getMessage());
                setVisibility(GONE);
            }
        });

        setVisibility(GONE); // hidden until onAdLoaded fires, so no layout jump
        addView(newAdView);
        adView = newAdView;
        newAdView.loadAd(new AdRequest.Builder().build());
    }

    /** Call from the host Activity/Fragment's onPause(). */
    public void pause() {
        if (adView != null) adView.pause();
    }

    /** Call from the host Activity/Fragment's onResume(). */
    public void resume() {
        if (adView != null) adView.resume();
    }

    /** Call from the host Activity/Fragment's onDestroy(). */
    public void destroyCurrentAd() {
        if (adView != null) {
            adView.destroy();
            removeView(adView);
            adView = null;
        }
    }
}