/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("loginbutton").addEventListener('click', (e) => {
    var form = e.target.closest("form");

    if (form.checkValidity()) {
      makeCall("POST", 'CheckLogin', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('username', message);
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("errormessage").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errormessage").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errormessage").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });
  
  document.getElementById("registerButton").addEventListener("click", (ev) => {

        const form = ev.target.closest("form");

        const password = form.querySelector("[name=registrationPassword]").value;
        const repeatedPassword = form.querySelector("[name=repeatedPassword]").value;
        const mail = form.querySelector("[name=email]").value;

        if (form.checkValidity() && isAGoodMail(mail) && samePassword(password, repeatedPassword)) {

            makeCall("POST", "Registration", form, function (request) {

                if (request.readyState === XMLHttpRequest.DONE) {
                    if(request.status === 200) {
						document.getElementById("title").textContent = "Your account has been created!\nLog in!";
                        window.location.href = "#";
                    } else {
                        alert(request.responseText);
                    }
                }
            });
        } else {
    	 form.reportValidity();
		}
    });

    function isAGoodMail(mail) {

        var mailPattern = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;

        if(mail.match(mailPattern))
            return true;
        else {
            alert("This Mail address is not valid!");
            return false;
        }
    }

    function samePassword (password, confirmPassword) {

        if (password == null || confirmPassword == null) {
            alert("Password can not be empty!");
            return false;
        }

        if (!(password  === confirmPassword)){
            alert("The passwords must be the same!");
            return password === confirmPassword;
        }
        return true;
    }

})();