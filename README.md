# RekijanAds

Standalone Android-project met daarin de gedeelde library-module
`rekijan-ads` (banner ad-bar + EU/EEA consent-flow), bedoeld om vanuit
meerdere losse app-projecten als Gradle-dependency binnengehaald te worden
via JitPack.

## Eenmalig: AdMob

1. Maak (of gebruik) een AdMob-account op https://admob.google.com.
2. Maak per app een "App"-entry aan → levert een **App ID** op
   (`ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy`).
3. Maak per app een **Ad Unit** aan van het type "Banner" → levert een
   **Ad Unit ID** op (`ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz`).

Deze twee ID's verschillen per app — de library zelf bevat er geen.

## Eenmalig: deze library publiceren via JitPack

1. Push dit hele project (root, met `rekijan-ads/` als submap) naar een
   eigen GitHub-repo (publiek, of privé als je JitPack Pro gebruikt).
2. Maak een git tag aan, bijv. `v1.0.0`, en push die.
3. JitPack bouwt de library pas wanneer iemand 'm voor het eerst opvraagt —
   check https://jitpack.io/#<jouw-username>/rekijan-ads om de build te
   triggeren en te verifiëren dat 'ie slaagt.

## Per app: dependency toevoegen

In de root `settings.gradle` van de app:

```gradle
dependencyResolutionManagement {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

In de `build.gradle` van de app-module:

```gradle
dependencies {
    implementation 'com.github.<jouw-username>:RekijanAds:v1.0.0'
}
```

(Let op: de repo-naam in deze coördinaat is de naam van je GitHub-repo,
niet per se de module-naam `rekijan-ads` — check dit op de JitPack-pagina
van je repo, die toont de exacte regel die je moet gebruiken.)

## Per app: AndroidManifest.xml

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy"/>
</application>
```

## Per app: gebruik in code

In de host Activity, vóór je een banner laadt of een rewarded ad opvraagt:

```java
AdConsentManager consentManager = new AdConsentManager(this);
consentManager.requestConsentThenInit(new AdConsentManager.Callback() {
    @Override
    public void onReady() {
        bottomAdBar.load("ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz");
    }

    @Override
    public void onFailure(String reason) {
        // Geen network, of consent geweigerd waar personalisatie vereist was.
        // Balk blijft simpelweg verborgen (GONE), app werkt gewoon door.
    }
});
```

In de layout-XML van het scherm:

```xml
<nl.rekijan.ads.RekijanBannerAdView
    android:id="@+id/bottomAdBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom" />
```

En koppel de lifecycle:

```java
@Override
protected void onPause() {
    super.onPause();
    bottomAdBar.pause();
}

@Override
protected void onResume() {
    super.onResume();
    bottomAdBar.resume();
}

@Override
protected void onDestroy() {
    super.onDestroy();
    bottomAdBar.destroyCurrentAd();
    super.onDestroy();
}
```

## Testen

Gebruik tijdens ontwikkeling Google's **test ad unit ID**
(`ca-app-pub-3940256099942544/6300978111` voor banners) in plaats van je
echte Ad Unit ID, zodat je niet per ongeluk op je eigen echte ads klikt
(wat tegen AdMob-beleid is).
