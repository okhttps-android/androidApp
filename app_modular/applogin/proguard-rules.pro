# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

###############################
#
# 公共部分（固定不变）
#
###############################
#1.基本指令区
# 代码混淆压缩比，在0~7之间，默认为5，一般不做修改
-optimizationpasses 5
# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames
# 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses
# 这句话能够使我们的项目混淆后产生映射文件
# 包含有类名->混淆后类名的映射关系
-verbose
# 指定不去忽略非公共库的类成员
-dontskipnonpubliclibraryclassmembers
# 不做预校验，preverify是proguard的四个步骤之一，Android不需要preverify，去掉这一步能够加快混淆速度。
-dontpreverify
# 保留Annotation不混淆
-keepattributes *Annotation*
-keepattributes InnerClasses
# 避免混淆反射
-keepattributes EnclosingMethod
# 避免混淆泛型
-keepattributes Signature
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*

#2.默认保留区
# 保留我们使用的四大组件，自定义的Application等等这些类不被混淆
# 因为这些子类都有可能被外部调用
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.IntentService
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
# 保留support下的所有类及其内部类
-keep class android.support.** {*;}
# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**
# 保留R下面的资源
-keep class **.R$* {*;}

# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
# 保留在Activity中的方法参数是view的方法，
# 这样以来我们在layout中写的onClick就不会被影响
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
# 保留枚举类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# 保留我们自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
}

#3.webview
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.webView, jav.lang.String);
}
#移除Log类打印各个等级日志的代码，打正式包的时候可以做为禁log使用
#这里可以作为禁止log打印的功能使用，另外的一种实现方案是通过BuildConfig.DEBUG的变量来控制
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** i(...);
    public static *** d(...);
    public static *** w(...);
    public static *** e(...);
}


###############################
#
# 第三方jar包
#
###############################
-keep class com.baidu.** {*;}
-keep class mapsdkvi.com.** {*;}
-dontwarn com.baidu.**

-keep class com.uuzuche.lib_zxing.** {*;}
-keep class com.andreabaccega.** {*;}
-keep class com.github.clans.fab.** {*;}
-keep class com.yalantis.phoenix.** {*;}
-keep class com.baoyz.swipemenulistview.** {*;}
-keep class com.viewpagerindicator.** {*;}
-keep class com.afollestad.materialdialogs.** {*;}
-keep class com.github.mikephil.charting.** {*;}
-keep class com.module.recyclerlibrary.** {*;}
-keep class com.handmark.pulltorefresh.library.** {*;}
-keep class cc.cloudist.acplibrary.** {*;}

-keep class android.arch.core.internal.** {*;}
-keep class android.arch.lifecycle.** {*;}
-keep class com.alibaba.fastjson.** {*;}
-dontwarn com.alibaba.fastjson.**
-keep class android.support.graphics.drawable.** {*;}
-keep class com.facebook.stetho.** {*;}
-keep class javax.annotation.** {*;}
-keep class com.lidroid.xutils.** {*;}
#okhttp3
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** {*;}
-dontwarn okio.**
#retrofit
-keep class com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn retrofit.**
-dontwarn rx.**
#retrofit2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep class org.apache.** {*;}
-keep class org.junit.** {*;}
-keep class junit.** {*;}
-keep class org.hamcrest.** {*;}

#design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

#support-v7-appcompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

#cardview
-keep class android.support.v7.widget.RoundRectDrawable { *; }

#gson
-keep class sun.misc.Unsafe {*;}
-keep class com.google.gson.examples.android.model.** {*;}
-keep class com.google.gson.** {*;}
-keep class com.google.gson.stream.** {*;}
-keep class com.android.volley.** {*;}

#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
   @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
   @butterknife.* <methods>;
}
-keep class com.nostra13.universalimageloader.** {*;}
-keep class cat.ereza.customactivityoncrash.** {*;}
-keep class uk.co.senab.photoview.** {*;}
-keep class com.flipboard.bottomsheet.** {*;}
-keep class flipboard.bottomsheet.** {*;}

#glide
-keep class com.bumptech.glide.** {*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#sharesdk
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-dontwarn cn.sharesdk.**
-dontwarn **.R$*
-dontwarn com.tencent.**
-keep class com.tencent.** {*;}

#android-gif-drawable
-keep class pl.droidsonroids.** {*;}
-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}
#retrolambda
-dontwarn java.lang.invoke.*
#rxjava
-keep class rx.** {*;}
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}

-keep class net.sourceforge.pinyin4j.**{*;}
-keep class org.xbill.** {*;}
-dontwarn org.xbill.**
-keep class com.chad.library.** {*;}
-keep class com.tonicartos.widget.stickygridheaders.** {*;}
-keep class com.orhanobut.logger.** {*;}
-keep class com.readystatesoftware.systembartint.** {*;}
-keep class com.scwang.smartrefresh.** {*;}
-keep class com.squareup.** {*;}
-keep class com.tencent.** {*;}
-keep class com.youth.banner.** {*;}
-keep class de.hdodenhof.circleimageview.** {*;}

-keep class org.joda.time.** {*;}
-dontwarn org.joda.time.**
-keep class me.gujun.android.taggroup.** {*;}
-keep class me.zhanghai.android.materialprogressbar.** {*;}
-keep class android.net.** {*;}
-keep class com.android.internal.http.multipart.** {*;}
-keep class se.emilsjolander.stickylistheaders.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.alipay.**
-keep class com.tencent.mm.sdk.** {*;}
-keep class com.loopj.android.http.**{*;}
-keep class com.novell.sasl.client.**{*;}
-keep class de.measite.smack.**{*;}
-keep class org.jivesoftware.**{*;}
-keep class flexjson.**{*;}
-keep class lecho.lib.hellocharts.**{*;}
-keep class org.apache.http.entity.mime.**{*;}
-keep class com.litesuits.orm.**{*;}
-keep class com.iflytek.**{*;}
-keep class com.nineoldandroids.**{*;}
-keep class org.xbill.DNS.**{*;}

#Ormlite
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-keep class * extends com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
-keepclassmembers class * extends com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper {
  public <init>(android.content.Context);
}
-keep @com.j256.ormlite.table.DatabaseTable class * {
    @com.j256.ormlite.field.DatabaseField <fields>;
    @com.j256.ormlite.field.ForeignCollectionField <fields>;
    # Add the ormlite field annotations that your model uses here
#    <init>();
}
#-keepclassmembers class * {
#    public <init>(android.content.Context);
#}

-keep class com.hp.hpl.sparta.**{*;}
-keep class pinyindb.**{*;}
-keep class demo.** {*;}
-dontwarn demo.**
#友盟
-keep class com.umeng.analytics.** {*;}
-keep class com.umeng.socialize.**{*;}
-keep class com.umeng.qqsdk.**{*;}
-keep class com.tencent.mm.sdk.**{*;}
-keep class core.umeng.wxsdk.**{*;}
-keep class com.umeng.customview.**{*;}
-keep class com.zhy.tree.bean.**{*;}

-keep class com.iflytek.sunflower.**{*;}
-keep class MTT.**{*;}

###############################
#
# 自己的代码
# 引用的其他Module可以直接在app的这个混淆文件里配置
#
###############################
-keep class com.core.net.** {*;}
-keep class com.core.dao.** {*;}
#实体类
-keep class com.modular.booking.model.** {*;}
-keep class com.uas.appcontact.model.** {*;}
-keep class com.modular.login.model.** {*;}
-keep class com.modular.booking.model.** {*;}
-keep class com.uas.appme.settings.model.** {*;}
-keep class com.modular.appmessages.model.** {*;}
-keep class com.modular.apputils.model.** {*;}
-keep class com.uas.appworks.model.** {*;}
-keep class com.xzjmyk.pm.activity.bean.** {*;}
-keep class com.modular.apputils.model.** {*;}
-keep class com.core.xmpp.model.** {*;}
-keep class com.core.model.** {*;}
-keep class com.core.app.** {*;}
-keep class com.uas.appworks.OA.erp.model.** {*;}
#自定义控件
-keep class com.core.widget.** {*;}
-keep class com.modular.booking.widget.** {*;}
-keep class com.uas.appcontact.ui.widget.stickylistheaders.** {*;}
-keep class com.uas.appme.widget.** {*;}
-keep class com.modular.appmessages.widget.** {*;}
-keep class com.modular.apputils.widget.** {*;}
-keep class com.uas.appworks.widget.** {*;}
-keep class com.ipaulpro.afilechooser.** {*;}
-keep class com.roamer.slidelistview.** {*;}
-keep class com.xzjmyk.pm.activity.view.** {*;}
-keep class com.xzjmyk.pm.activity.video.** {*;}
-keep class com.modular.apputils.utils.** {*;}
#与js互相调用的类
-keep class com.com.xzjmyk.pm.activity.ui.erp.activity.WebViewCommActivity.JSWebView
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}

# keep annotated by NotProguard
# 被NotProguard注解过的类或方法或属性不被混淆
#-keep @com.core.base.NotProguard class * {*;}
#-keep class * {
#    @com.core.base.NotProguard <fields>;
#}
#-keepclassmembers class * {
#    @com.core.base.NotProguard <methods>;
#}

