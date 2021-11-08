# Android组件库

[基础库MagpieX](https://github.com/zhouzhuo810/MagpieX),以下组件清单中组件均以px作为单位进行尺寸配置，因此屏幕适配依赖基础库进行适配

## 组件清单
#### 1. [二维码](zxing)
#### 2. [图表](MPChartLib)
#### 3. [日历](CalendarPickerView)
#### 4. [索引](indexbar)
#### 5. [面包屑](breadcrumb)
#### 6. [倒计时](countdownview)
#### 7. [数据选择](PickerView)
#### 8. [进度条](progressbar)
#### 9. [彩虹条](rainbowbar)
#### 10. [滑动条](seekbar)
#### 11. [定制的简单Views](views)
#### 12. [RV适配器](brvah)
#### 13. [Layout库](layout)
#### 14. [华为扫码](huaweiscan)


## 其它库
#### 1. [图片预览](https://github.com/wanggaowan/PhotoPreview)
#### 2. [表格](https://github.com/wanggaowan/TableLite)
#### 3. [多图展示](https://github.com/zhouzhuo810/ZzImageBox)
#### 4. [阴影库](https://github.com/lihangleo2/ShadowLayout)


---
## 集成

[![](https://jitpack.io/v/HZKQ/Widgets.svg)](https://jitpack.io/#HZKQ/Widgets)

项目级build.gradle文件添加
 ```
 allprojects {
    repositories {
       maven { url 'https://jitpack.io' }
       ...
       // 集成华为扫码还需配置HMS Core SDK的Maven仓地址
       maven { url 'https://developer.huawei.com/repo/' }
    }
}
 ```

集成以上所有组件：
```groovy
implementation 'com.github.HZKQ:Widgets:latest.release.here'
```

分库集成
```groovy
// 二维码
implementation 'com.github.HZKQ.Widgets:zxing:latest.release.here'

// 图表
implementation 'com.github.HZKQ.Widgets:MPChartLib:latest.release.here'

// 日历
implementation 'com.github.HZKQ.Widgets:CalendarPickerView:latest.release.here'

// 索引
implementation 'com.github.HZKQ.Widgets:indexbar:latest.release.here'

// 面包屑
implementation 'com.github.HZKQ.Widgets:breadcrumb:latest.release.here'

// 倒计时
implementation 'com.github.HZKQ.Widgets:countdownview:latest.release.here'

// 数据选择
implementation 'com.github.HZKQ.Widgets:PickerView:latest.release.here'

// 进度条
implementation 'com.github.HZKQ.Widgets:progressbar:latest.release.here'

// 彩虹条
implementation 'com.github.HZKQ.Widgets:rainbowbar:latest.release.here'

// 滑动条
implementation 'com.github.HZKQ.Widgets:seekbar:latest.release.here'

// 定制的简单Views
implementation 'com.github.HZKQ.Widgets:views:latest.release.here'

// RV适配器
implementation 'com.github.HZKQ.Widgets:brvah:latest.release.here'

// 布局库
implementation 'com.github.HZKQ.Widgets:layout:latest.release.here'

// 华为统一扫码
implementation 'com.github.HZKQ.Widgets:huaweiscan:latest.release.here'
```

## ProGuard
```
// 布局库
-keep class com.keqiang.layout.combination.** {*;}

// 华为统一扫码
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
```

## 使用说明
1. 建议下载[demoApk](./app-debug.apk)查看各组件效果
2. 具体使用说明，查阅[sample](sample) demo

---
> ## Layout库
> 该库主要由LazyColumn、LazyRow、AdapterView、GroupPlaceholder组成
> 
>>### LazyColumn、LazyRow
>> Layout库主要控件，用于组合添加到其下的LazyColumn、LazyRow、AdapterView、GroupPlaceholder
>>
>>### AdapterView
>> 用于处理列表数据，用法与RecyclerView相似，且增加了isolateViewTypes和type_flag属性，用于配置多个AdapterView item布局复用。内部不可添加其它视图，且只有作为LazyColumn、LazyRow、GroupPlaceholder 直接子节点时，才会执行预期逻辑，否则就是一个普通的View控件
>>
>>### GroupPlaceholder
>> 组视图的占位符，用于整体处理一组View的添加、移除、滚动等操作，其内部可添加其它View(如：LazyColumn、LazyRow、AdapterView、GroupPlaceholder)，实际运行时，子节点向上提升，当前View不展示
>>
>> 使用方式如下(优点在于多个列表可整体滚动，且需要给列表加上头部，底部，多个列表直接增加分隔符或其它内容，都可简单通过xml配置，且可以一目了然的预览)，详细使用请参考[Demo](./sample/src/main/java/com/hzkq/widgets/layout):
>>```xml
>>···
>><com.keqiang.layout.combination.LazyColumn
>>        android:layout_width="match_parent"
>>        android:layout_height="match_parent"
>>        android:background="#ff61b4fe">
>>
>>        <TextView
>>            android:layout_width="match_parent"
>>            android:layout_height="100dp"
>>            android:background="#fffb9a55"
>>            android:gravity="center"
>>            android:text="顶部View"
>>            android:textSize="20sp" />
>>
>>        <TextView
>>            android:layout_width="match_parent"
>>            android:layout_height="40dp"
>>            android:layout_marginTop="10dp"
>>            android:background="#DDD3CC"
>>            android:gravity="center_vertical"
>>            android:paddingStart="20dp"
>>            android:paddingEnd="20dp"
>>            android:text="列表"
>>            android:textSize="14sp" />
>>
>>        <com.keqiang.layout.combination.AdapterView
>>            android:id="@+id/lazyColumn"
>>            android:layout_width="match_parent"
>>            android:layout_height="wrap_content"
>>            android:background="@color/colorBlack"
>>            app:itemCount="2"
>>            app:layout="@layout/rv_item_layout" />
>>
>>        <TextView
>>            android:layout_width="match_parent"
>>            android:layout_height="100dp"
>>            android:background="#fffb9a55"
>>            android:gravity="center"
>>            android:text="底部View"
>>            android:textSize="20sp" />
>>
>>    </com.keqiang.layout.combination.LazyColumn>
>>···
>>```
---
>## 华为统一扫码
>```java
>// 扫码
>CodeUtils.scan(context, scanResult -> {
>        if (scanResult.isCancel()) {
>            return;
>        }
>        
>        HmsScan data = scanResult.getData();
>        if (data == null || scanResult.getContents() == >null) {
>            ToastUtil.showToast("未识别到内容");
>            return;
>        }
>        
>        String contents = scanResult.getContents();
>        ToastUtil.showToast(contents);
>    });
>
>// 生成二维码
>CodeUtils.createQrCode("xxx",200,200);
>
>// 生成条码
>CodeUtils.createBarCode("xxx",400,100);
>```
>
>如果不改变扫码界面样式，仅改变部分默认文本或颜色，则覆盖对应属性即可
>```xml
>// 文本
><string name="scan_code_title_label">扫码</string>
>< name="scan_code_hint">请将方框对准二维码/条码开始扫描</>string>
><string name="album_label">相册</string>
>< name="image_scan_error_hint">未识别二维码/条形码</>string>
>
>// 颜色
><color name="colorPrimary">#3A559B</color>
><color name="scan_line_color">#5A3A559B</color>
>```
>
>如果要修改界面样式，覆盖布局```activity_huawei_scan.xml```即可
---



