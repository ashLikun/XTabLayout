
[![Release](https://jitpack.io/v/ashLikun/XTabLayout.svg)](https://jitpack.io/#ashLikun/XTabLayout)

# **XTabLayout**
    基于官方的Tablayout重写
## 使用方法

build.gradle文件中添加:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
并且:

```gradle
dependencies {
    implementation 'com.github.ashLikun:XTabLayout:{latest version}'
    
    //如果不使用内部的Android  designVersion（27.1.1） 可以这样
    implementation ('com.github.ashLikun:XTabLayout:{latest version}'){
        exclude group: 'com.android.support'
    }
     
}
```
### 1.用法
     与官方的一样使用，多了一些属性
```xml
       <resources>
           <declare-styleable name="XTabLayout">
               <!--指示器高度-->
               <attr name="xTabIndicatorHeight" format="dimension" />
               <!--指示器宽度-->
               <attr name="xTabIndicatorWidth" format="dimension" />
               <!--指示器颜色-->
               <attr name="xTabIndicatorColor" format="color" />
               <!--指示器宽度和字体一样-->
               <attr name="xTabIndicatorWidthWidthText" format="boolean" />
               <!--内边距-->
               <attr name="xTabPadding" format="dimension" />
               <attr name="xTabPaddingStart" format="dimension" />
               <attr name="xTabPaddingTop" format="dimension" />
               <attr name="xTabPaddingEnd" format="dimension" />
               <attr name="xTabPaddingBottom" format="dimension" />
               <!--文字的属性-->
               <attr name="xTabTextAppearance" format="reference" />
               <!--文字默认颜色-->
               <attr name="xTabTextColor" format="color" />
               <!--文字选择颜色-->
               <attr name="xTabSelectedTextColor" format="color" />
               <!--最小宽度-->
               <attr name="xTabMinWidth" format="dimension" />
               <!--最大宽度-->
               <attr name="xTabMaxWidth" format="dimension" />
               <!--背景色-->
               <attr name="xTabBackgroundColor" format="reference|color" />
               <!--选中的背景色-->
               <attr name="xTabSelectedBackgroundColor" format="reference|color" />
               <!--内容前面边距-->
               <attr name="xTabContentStart" format="dimension" />
               <!--排版模式-->
               <attr name="xTabMode">
                   <enum name="scrollable" value="0x00000000" />
                   <enum name="fixed" value="0x00000001" />
                   <enum name="auto" value="0x00000002" />
               </attr>
               <!--对齐方式-->
               <attr name="xTabGravity" format="integer" />
               <!--文字大小-->
               <attr name="xTabTextSize" format="dimension" />
               <!--文字加粗-->
               <attr name="xTabTextBold" format="boolean" />
               <!--选中文字大小-->
               <attr name="xTabSelectedTextSize" format="dimension" />
               <!--选中文字加粗-->
               <attr name="xTabTextSelectedBold" format="boolean" />
               <!--一页显示几个item（当N个内容没有一屏的时候有效）-->
               <attr name="xTabDisplayNum" format="integer" />
               <!--文本字母是否小写转大写-->
               <attr name="xTabTextAllCaps" format="boolean" />
               <!--分割线高度-->
               <attr name="xTabDividerHeight" format="dimension" />
               <!--分割线宽度-->
               <attr name="xTabDividerWidth" format="dimension" />
               <!--分割线颜色-->
               <attr name="xTabDividerColor" format="color" />
               <!--分割线对齐方式-->
               <attr name="xTabDividerGravity">
                   <enum name="top" value="0x00000000" />
                   <enum name="center" value="0x00000001" />
                   <enum name="bottom" value="0x00000002" />
               </attr>
           </declare-styleable>
       </resources>   
```
### 2版本
#### 1.0.0
    1：指示器宽度可以和文字一样宽，也可以固定大小
    2：新增模式模式AUTO，可以自动适配宽度
    3：多了分割线
    4：选中的背景可以改变
### 混肴
####


