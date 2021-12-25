package com.example.hendrik.mianamalaga.container;

public class ChatContent {
    private String[] nativeMessages;
    private String translatedMessage;
    private String[] mediaFileNames;
    private String imageFileName;
    private boolean isUser;
    private String supplementalInfoText;
    private int jumpIndex;

    public ChatContent(){
        nativeMessages = new String[]{"","",""};
        translatedMessage = "";
        mediaFileNames = new String[]{""};
        imageFileName = "";
        supplementalInfoText = "";
        jumpIndex = 1;
    }

    public String[] getNativeMessages() {
        return nativeMessages;
    }

    public void setNativeMessages(String[] nativeMessages) {
        this.nativeMessages = nativeMessages;
    }

    public String getTranslatedMessage() {
        return translatedMessage;
    }

    public void setTranslatedMessages(String translatedMessage) {
        this.translatedMessage = translatedMessage;
    }

    public String[] getMediaFileNames() {
        return mediaFileNames;
    }

    public void setMediaFileNames(String[] mediaFileNames) {
        this.mediaFileNames = mediaFileNames;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public int getJumpIndex() {
        return jumpIndex;
    }

    public void setJumpIndex(int jumpIndex) {
        this.jumpIndex = jumpIndex;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getSupplementalInfoText(){ return  supplementalInfoText; }

    public void setSupplementalInfoText(String infoText ) { this.supplementalInfoText = infoText; }
}


/*

<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_conversation_drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".activities.ActivityConversation">

<RelativeLayout
    android:id="@+id/conversation_main_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/cloud_background">

    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/appbar_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:layout_marginBottom="6dp">
        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar_conversation"
            android:layout_width="match_parent"
            android:layout_height="@dimen/my_app_bar_height"
            android:background="@color/primaryColor"
            app:titleMarginStart="24dp"
            app:titleTextAppearance="@style/Toolbar.TitleText"/>
    </com.google.android.material.appbar.AppBarLayout>

    <androidx.cardview.widget.CardView
        android:id="@+id/video_view_card_view"
        android:layout_width="150dp"
        android:layout_height="150dp"
        android:layout_centerHorizontal="true"
        android:layout_centerInParent="true"
        android:elevation="52dp"
        android:innerRadius="10dp"
        android:shape="ring"
        android:thicknessRatio="1.9"
        android:visibility="invisible"
        app:cardCornerRadius="75dp">

        <VideoView
            android:id="@+id/conversation_video_view"
            android:layout_width="320dp"
            android:layout_height="320dp"
            android:scaleType="fitXY"
            android:layout_centerHorizontal="true"
            android:layout_gravity="center"
            android:keepScreenOn="true"
            android:orientation="horizontal"
            android:visibility="invisible"/>

        <ImageView
            android:id="@+id/conversation_video_image_view"
            android:layout_width="150dp"
            android:layout_height="150dp"
            android:layout_centerHorizontal="true"
            android:layout_gravity="center"
            android:keepScreenOn="true"
            android:scaleType="fitXY"
            android:orientation="horizontal"
            android:visibility="invisible"/>

    </androidx.cardview.widget.CardView>

    <androidx.cardview.widget.CardView
        android:id="@+id/camera_view_card_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="100dp"
        android:layout_gravity="center">

        <FrameLayout
            android:id="@+id/container"
            android:layout_width="200dp"
            android:layout_height="240dp"
            android:layout_centerHorizontal="true"
            android:layout_gravity="center"
            android:layout_marginStart="8dp"
            android:layout_marginLeft="8dp"
            android:layout_marginTop="8dp"
            android:layout_marginEnd="8dp"
            android:layout_marginRight="8dp"
            android:layout_marginBottom="15dp"
            android:keepScreenOn="true"
            android:orientation="horizontal"
            android:visibility="gone">
        </FrameLayout>

    </androidx.cardview.widget.CardView>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/conversation_listView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="50dp"
        android:layout_alignParentTop="true"
        android:layout_above="@id/pager_card_view"
        android:scrollbars="vertical">
    </androidx.recyclerview.widget.RecyclerView>

    <androidx.cardview.widget.CardView
        android:id="@+id/pager_card_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_above="@id/response_editText_layout"
        android:layout_marginLeft="8dp"
        android:layout_marginTop="4dp"
        android:layout_marginRight="8dp"
        android:layout_marginBottom="4dp">

        <TextView
            android:id="@+id/solution_textView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Solution"
            android:textAlignment="center"
            android:gravity="center_horizontal">
        </TextView>

        <androidx.viewpager.widget.ViewPager
            android:id="@+id/pager"
            android:layout_width="match_parent"
            android:layout_height="25dp"
            android:visibility="gone"
            ></androidx.viewpager.widget.ViewPager>

    </androidx.cardview.widget.CardView>

    <Button
        android:id="@+id/buttonA"
        android:visibility="visible"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_above="@id/buttonB"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true"
        android:layout_alignParentLeft="true"
        android:text="Button A" />

    <Button
        android:id="@+id/buttonB"
        android:visibility="visible"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_above="@id/buttonC"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true"
        android:layout_alignParentLeft="true"
        android:text="Button B" />

    <Button
        android:id="@+id/buttonC"
        android:visibility="visible"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignBottom="@+id/conversation_listView"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true"
        android:layout_alignParentLeft="true"
        android:text="Button C" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/response_editText_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentStart="true"
        android:layout_alignParentRight="true"
        android:layout_alignParentBottom="true"
        android:layout_alignParentLeft="true">

        <EditText
            android:id="@+id/response_editText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/type_your_answer"
            android:imeOptions="actionDone"
            android:inputType="text"
            android:layout_marginRight="40dp"
            android:paddingLeft="@dimen/activity_horizontal_margin"
            android:paddingTop="@dimen/activity_vertical_margin"
            android:paddingRight="@dimen/activity_horizontal_margin"
            android:paddingBottom="@dimen/activity_vertical_margin"/>

    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/conversation_fab_add"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true"
        android:layout_alignParentBottom="true"
        android:layout_marginEnd="60dp"
        android:layout_marginRight="60dp"
        android:layout_marginBottom="60dp"
        android:backgroundTint="@color/secondaryColor"
        android:clickable="true"
        android:foregroundGravity="bottom|right"
        android:src="@drawable/ic_plus"
        android:tint="#ffffff"
        app:fabSize="mini"/>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/conversation_fab_remove"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true"
        android:layout_alignParentBottom="true"
        android:layout_marginEnd="60dp"
        android:layout_marginRight="60dp"
        android:layout_marginBottom="15dp"
        android:backgroundTint="@color/secondaryColor"
        android:clickable="true"
        android:foregroundGravity="bottom|right"
        android:src="@drawable/ic_minus"
        android:tint="#ffffff"
        app:fabSize="mini"/>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/conversation_fab_add_user_message"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true"
        android:layout_alignParentBottom="true"
        android:layout_marginEnd="7dp"
        android:layout_marginRight="7dp"
        android:layout_marginBottom="60dp"
        android:backgroundTint="@color/secondaryColor"
        android:clickable="true"
        android:foregroundGravity="bottom|right"
        android:src="@drawable/ic_add_simple"
        android:tint="#ffffff"
        app:fabSize="mini"/>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_help"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true"
        android:layout_alignParentBottom="true"
        android:layout_marginEnd="7dp"
        android:layout_marginRight="7dp"
        android:layout_marginBottom="15dp"
        android:backgroundTint="@color/secondaryColor"
        android:clickable="true"
        android:foregroundGravity="bottom|right"
        android:src="@drawable/ic_help"
        android:tint="#ffffff"
        app:fabSize="mini"/>




</RelativeLayout>

    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        android:fitsSystemWindows="true"
        app:headerLayout="@layout/navigation_drawer_header"
        app:menu="@menu/menu_conversation" />


</androidx.drawerlayout.widget.DrawerLayout>



 */