# Cordova Plugin AndroidKeyboard

<!-- [![Android Testsuite](https://github.com/SinGlEBW/cordova-plugin-android-keyboard.git)](https://github.com/SinGlEBW/cordova-plugin-android-keyboard.git)  -->



## Installation

This installation method requires cordova 10.0+

    cordova plugin add cordova-plugin-android-keyboard

<!-- It is also possible to install via repo url directly ( unstable )

    cordova plugin add https://github.com/apache/cordova-plugin-statusbar.git -->


### Android Example


Use plugin cordova-plugin-statusbar and use overlaysWebView = true and config.xml <preference name="StatusBarOverlaysWebView" value="true" />;


If you need an event to open and close the keyboard, then use:

```js
  if (cordova.platformId == 'android') {
    let AndroidKeyboard = cordova.plugins.AndroidKeyboard;

    AndroidKeyboard.on('keyboard', (data) => {
      console.dir(data); //data = { isShow: boolean; height: number;  }
    });
    //remove event
    AndroidKeyboard.off('keyboard');
  }
```