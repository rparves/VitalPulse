# SQLCipher uses JNI; keep its classes intact.
-keep class net.zetetic.database.** { *; }
-keep class net.zetetic.database.sqlcipher.** { *; }
# Strip verbose/debug logging from release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Room entities/DAOs are generated code; keep entity fields for reflection-free safety.
-keep class com.vitapulse.app.data.** { *; }
# Health Connect record classes are referenced reflectively by the client library.
-keep class androidx.health.connect.client.records.** { *; }
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
