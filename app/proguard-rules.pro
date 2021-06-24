# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

-dontwarn android.support.**
-keep class * extends androidx.support.v4.app.Fragment{}
-keep class * extends android.support.v4.app.Fragment{}
-keep class  androidx.navigation.fragment.NavHostFragment.** { *; }
-keep class com.bink.wallet.model.** { *; }
-keep class com.bink.wallet.modal.** { *; }
-keep class com.bink.wallet.utils.** { *; }
-keep class com.bink.wallet.utils.enums.** { *; }
-keep public class androidx.support.v7.widget.** { *; }
-keep public class android.support.v7.widget.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Guide Main Screen
-keep class zendesk.support.HelpCenterSettings { *; }
-keep class zendesk.support.HelpResponse { *; }
-keep class zendesk.support.ArticlesListResponse { *; }
-keep class zendesk.support.CategoryItem { *; }
-keep class zendesk.support.SectionItem { *; }
-keep class zendesk.support.ArticleItem { *; }
-keep class zendesk.support.SeeAllArticlesItem { *; }
-keep class zendesk.support.guide.HelpCenterActivity { *; }

# Guide Search Results
-keep class zendesk.support.guide.HelpSearchFragment { *; }
-keep class zendesk.support.ArticlesSearchResponse { *; }
-keep class zendesk.support.SearchArticle { *; }
-keep class zendesk.support.guide.HelpSearchRecyclerViewAdapter { *; }
-keep class zendesk.support.HelpCenterSearch { *; }
-keep class zendesk.support.Category { *; }
-keep class zendesk.support.Section { *; }
-keep class zendesk.support.Article { *; }

# Guide View Article
-keep class zendesk.support.guide.ArticleViewModel { *; }
-keep class zendesk.support.guide.ArticleConfiguration { *; }
-keep class zendesk.support.guide.ViewArticleActivity { *; }
-keep class zendesk.support.ArticleResponse { *; }
-keep class zendesk.support.ArticleVote { *; }
-keep class zendesk.support.ArticleVoteResponse { *; }
-keep class zendesk.support.ZendeskArticleVoteStorage { *; }
-keep class zendesk.support.AttachmentResponse { *; }
-keep class zendesk.support.HelpCenterAttachment { *; }

# Support Requests (Create, Update, List)
-keep class zendesk.support.request.** { *; }
-keep class zendesk.support.requestlist.** { *; }
-keep class zendesk.support.SupportSdkSettings { *; }
-keep class zendesk.support.Request { *; }
-keep class zendesk.support.CreateRequest { *; }
-keep class zendesk.support.Comment { *; }
-keep class zendesk.support.CommentResponse { *; }
-keep class zendesk.support.CommentsResponse { *; }
-keep class zendesk.support.EndUserComment { *; }
-keep class zendesk.support.ZendeskRequestStorage { *; }
-keep class zendesk.support.ZendeskRequestProvider { *; }
-keep class zendesk.support.CreateRequestWrapper { *; }
-keep class zendesk.support.UpdateRequestWrapper { *; }
-keep class zendesk.support.RequestsResponse { *; }
-keep class zendesk.support.RequestResponse { *; }

# Support Attachments
-keep class zendesk.support.UploadResponse { *; }
-keep class zendesk.support.UploadResponseWrapper { *; }
-keep class zendesk.support.ZendeskUploadProvider { *; }
-keep class zendesk.support.Attachment { *; }