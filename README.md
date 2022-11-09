
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
              <attr name="tabIndicatorHeight" format="dimension" />
              <!--指示器宽度-->
              <attr name="tabIndicatorWidth" format="dimension" />
              <!--指示器颜色-->
              <attr name="tabIndicatorColor" format="color" />
              <!--指示器宽度和字体一样-->
              <attr name="tabIndicatorWidthWidthText" format="boolean" />
              <!--内边距-->
              <attr name="tabPadding" format="dimension" />
              <attr name="tabPaddingStart" format="dimension" />
              <attr name="tabPaddingTop" format="dimension" />
              <attr name="tabPaddingEnd" format="dimension" />
              <attr name="tabPaddingBottom" format="dimension" />
              <!--文字的属性-->
              <attr name="tabTextAppearance" format="reference" />
              <!--文字默认颜色-->
              <attr name="tabTextColor" format="color" />
              <!--文字选择颜色-->
              <attr name="tabSelectedTextColor" format="color" />
              <!--最小宽度-->
              <attr name="tabMinWidth" format="dimension" />
              <!--最大宽度-->
              <attr name="tabMaxWidth" format="dimension" />
              <!--item背景色-->
              <attr name="tabItemBackground" format="reference|color" />
              <!--item选中的背景色-->
              <attr name="tabItemSelectedBackground" format="reference|color" />
              <!--内容前面边距-->
              <attr name="tabContentStart" format="dimension" />
              <!--排版模式-->
              <attr name="tabMode">
                  <enum name="scrollable" value="0x00000000" />
                  <enum name="fixed" value="0x00000001" />
                  <enum name="auto" value="0x00000002" />
              </attr>
              <!--对齐方式-->
              <attr name="tabGravity" format="integer" />
              <!--文字大小-->
              <attr name="tabTextSize" format="dimension" />
              <!--文字加粗-->
              <attr name="tabTextBold" format="boolean" />
              <!--选中文字大小-->
              <attr name="tabSelectedTextSize" format="dimension" />
              <!--选中文字加粗-->
              <attr name="tabTextSelectedBold" format="boolean" />
              <!--一页显示几个item（当N个内容没有一屏的时候有效）-->
              <attr name="tabDisplayNum" format="integer" />
              <!--文本字母是否小写转大写-->
              <attr name="tabTextAllCaps" format="boolean" />
              <!--分割线高度-->
              <attr name="tabDividerHeight" format="dimension" />
              <!--分割线宽度-->
              <attr name="tabDividerWidth" format="dimension" />
              <!--分割线颜色-->
              <attr name="tabDividerColor" format="color" />
              <!--分割线对齐方式-->
              <attr name="tabDividerGravity">
                  <enum name="top" value="0x00000000" />
                  <enum name="center" value="0x00000001" />
                  <enum name="bottom" value="0x00000002" />
              </attr>
          </declare-styleable>
      </resources>
```
###java 
 XTabLayoutMediator(tabLayout,fragmentLayout,adapter).attach

            XTabLayoutMediator:与TabLayout一起使用
### 2版本
#### 1.0.5
    优化api
    1：提供可设置Item选中和不选中的Drawable
    2：可设置滚动到指定的位置
    3: 回调方法优化position-1
#### 1.0.0
    1：指示器宽度可以和文字一样宽，也可以固定大小
    2：新增模式模式AUTO，可以自动适配宽度
    3：多了分割线
    4：选中的背景可以改变
### 混肴
####


