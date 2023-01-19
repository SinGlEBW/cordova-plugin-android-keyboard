var exec = require('cordova/exec');



let classInJava = 'AndroidKeyboard';
let AndroidKeyboard = {
    
    // hide: function () {
    //     exec(null, null, classInJava, 'hide', []);
    // },

    // show: function () {
    //     exec(null, null, classInJava, 'show', []);
    // },

    // getHeight: function (cb) {
    //     exec(cb, null, classInJava, 'getHeight', []);
    // },

    on: function(nameEvent, cbSuccess) {
        // let success = function(message) {   cbSuccess(message);  };

        if(nameEvent === 'keyboard'){
            exec(cbSuccess, null, classInJava, "on", []);
        }

    },
    off: function(nameEvent){
        if(nameEvent === 'keyboard'){
            exec(null, null, classInJava, "off", []);
        }
    
    } 
}


module.exports = AndroidKeyboard;

