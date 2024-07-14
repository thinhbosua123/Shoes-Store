$(document).ready(function () {
    $("#loginForm").submit(function (e) {
        e.preventDefault();
    }).validate({
        rules: {
            login_email: {
                required: true,
                email: true,
                maxlength: 50
            },
            login_password: {
                required: true,
                rangelength: [6, 20]
            }
        },
        messages: {
            login_email: {
                required: "Vui lòng nhập email!",
                email: "Email không đúng định dạng!",

            },
            login_password: {
                required: "Vui lòng nhập mật khẩu!",
                rangelength: "Mật khẩu có độ dài từ 6-20 ký tự!",
            }
        },

        submitHandler: function () {
            let email = $("#login_email").val();
            let password = $("#login_password").val();

            req = {
                email: email,
                password: password
            }
            let myJSON = JSON.stringify(req);
            $.ajax({
                url: '/api/login',
                type: 'POST',
                data: myJSON,
                contentType: "application/json; charset=utf-8",
                success: function(data) {
                    toastr.success("Đăng nhập thành công");
                    signedValidate(true, data.fullName);
                    $('.modal').modal('hide');
                },
                error: function(error) {
                    toastr.warning(error.responseJSON.message);
                },
            });
        }

    });

    $("#registerForm").submit(function (e) {
        e.preventDefault();
    }).validate({
        rules: {
            register_full_name: {
                required: true,
                maxlength: 25
            },
            register_phone: {
                required: true,
                phone: true
            },
            register_email: {
                required: true,
                email: true,
                maxlength: 25
            },
            register_password: {
                required: true,
                rangelength: [6, 25]
            },
            register_confirm_password: {
                required: true,
                equalTo: "#register_password",
                rangelength: [6, 25]
            }
        },
        messages: {
            register_full_name: {
                required: "Vui lòng nhập đầy đủ họ và tên!",
                maxlength: "Tên có độ dài tối đa 25 ký tự!",

            },
            register_phone: {
                required: "Vui lòng nhập số điện thoại!",
            },
            register_email: {
                required: "Vui lòng nhập email!",
                email: "Email không đúng định dạng!",
                maxlength: "Email có độ dài tối đa 25 ký tự!",
            },
            register_password: {
                required: "Vui lòng nhập mật khẩu!",
                rangelength: "Mật khẩu có độ dài từ 6-20 ký tự!"
            },
            register_confirm_password: {
                required: "Vui lòng nhập lại mật khẩu!",
                equalTo: "Mật khẩu không trùng nhau!",
                rangelength: "Mật khẩu có độ dài từ 6-20 ký tự!"
            }
        },

        submitHandler: function () {
            let fullName = $("#register_full_name").val();
            let phone = $("#register_phone").val();
            let email = $("#register_email").val();
            let password = $("#register_password").val();

            req = {
                fullName: fullName,
                email: email,
                password: password,
                phone: phone
            }
            var myJSON = JSON.stringify(req);
            $.ajax({
                url: '/api/register',
                type: 'POST',
                data: myJSON,
                contentType: "application/json; charset=utf-8",
                success: function(data) {
                    toastr.success("Đăng ký thành công");
                    signedValidate(true, data.fullName);
                    $('.modal').modal('hide');
                },
                error: function(error) {
                    toastr.warning(error.responseJSON.message);
                },
            });
        }
    })
});

$.validator.addMethod("phone", function (value, element) {
    return this.optional(element) || /((09|03|07|08|05)+([0-9]{8})\b)/g.test(value);
}, "Số điện thoại không hợp lệ!")

function signedValidate(status = false, fullname = '') {
    if (status == true) {
        isLogined = true;
        let signedLink = `
     <a href="/tai-khoan" id="account-setting">Xin chào ${fullname}</a>`;
        $('.account-setting').replaceWith(signedLink);
    } else {
        isLogined = false;
        let notSignedLink = `
              <a href="#" data-toggle="modal" data-target="#exampleModal" class="header-icon account-setting"><i class="icon-user-2"></i></a>
          `;
        $('.account-setting').replaceWith(notSignedLink);
    }
}

$(document).on('keyup', function (e) {
    let target = e.target;

    if (target.closest('.search-input')) {
        var keycode = (e.keyCode ? e.keyCode : e.which);
        if(keycode == '13'){
            searchProductByKeyword();
        }
    }
})


$('.search-button').click(function() {
    searchProductByKeyword();
})

function searchProductByKeyword() {
    let keyword = $('.search-input').val();
    if (keyword.length == 0) {
        toastr.warning("Vui lòng nhập từ khóa tìm kiếm");
        return
    }
    location.href="/api/tim-kiem?keyword="+keyword;
}