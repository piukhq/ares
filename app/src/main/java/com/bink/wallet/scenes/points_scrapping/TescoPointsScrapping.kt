package com.bink.wallet.scenes.points_scrapping

import android.widget.EditText

object TescoPointsScrapping {

    fun tescoLogin(et_email: EditText, et_password: EditText): String {
        return """javascript: (function() {
                        var userInput = document.getElementById('username');
                        userInput.value = '${et_email.text}';
                        document.getElementById('password').value = '${et_password.text}';
                        
                        var buttons = document.getElementsByClassName('ui-component__button');
                        var signInButton = buttons[0];
                        if(signInButton == null){
                           return
                        }
                          signInButton.click();
                    })(); """
    }


    fun getClubCardPoints(): String {
        return "(function() { return (document.getElementsByClassName('pointvalue')[0].innerHTML); })();"
    }
}