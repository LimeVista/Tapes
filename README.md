# Tapes
A Non-SQL-Like Storage for Java/Kotlin on Android.

## Initialize
* `Tapes.init(context)` should be called UI thread.
```java
// init it in the function of onCreate in Application or Activity.
Tapes.init(context);
```

## Write Data
Save an object.
```java
List<Data> content = ...
Tapes.track().write("content", content);
```

## Read Data
Read an object.
```java
// If key(content) not exist, the content value is null. 
List<Data> content = Tapes.track().read("content");

// Set default value.The method Null Safe.
List<Data> content = Tapes.track().read("content", new ArrayList<Data>());
```

## Delete Data by Key
```java
Tapes.track().clear("content");
```

## Get all keys
```java
//NonNull
List<String> allKey = Tapes.track().getAllKey();
```

## Default Track
```java
// Get default Track    
Track track = Tapes.track();
// or 
Track track = Tapes.track(null);
// or
Track track = Tapes.track(TAPES.INNER_DB_NAME);
```

## Use custom track
```java
Track track = Tapes.track("custom_track");
```

## More API
* See <a href="https://github.com/LimeVista/Tapes/blob/master/tapesdb/src/main/java/me/limeice/tapesdb/Tapes.java">Tapes</a> or <a href="https://github.com/LimeVista/Tapes/blob/master/tapesdb/src/main/java/me/limeice/tapesdb/Track.java">Track</a>

## Proguard
```groovy
-keep class your.app.data.** { *; }
# if use implement Java Serializable
-keep class * implements java.io.Serializable { *; }
```
## The Library Base On <a href="https://github.com/EsotericSoftware/kryo">Kryo</a>

## License
<a href="https://github.com/LimeVista/Tapes/blob/master/LICENSE">Apache LicenseVersion 2.0, January 2004</a>
