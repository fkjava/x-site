<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>登录</title>    <!-- Bootstrap core CSS -->
    <link rel="stylesheet" href="/webjars/bootstrap/3.3.7/dist/css/bootstrap.css"/>
    <script type="text/javascript" src="/webjars/jquery/3.3.1/dist/jquery.min.js"></script>
    <script type="text/javascript" src="/webjars/bootstrap/3.3.7/dist/js/bootstrap.min.js"></script>
    <style type="text/css">
        .addon-button {
            cursor: pointer;
        }
    </style>
</head>
<body>
<div class="col-sm-12 col-md-6 col-md-offset-3 text-center">
    <div class="alert alert-success hide" role="alert" id="succeed"></div>
    <c:if test="${param.logout eq '' }">
        <div class="alert alert-success" role="alert">成功退出登录</div>
    </c:if>
    <c:if test="${param.error eq '' or not empty sessionScope['SPRING_SECURITY_LAST_EXCEPTION'] }">
        <div class="alert alert-danger" role="alert">
            <strong>登录失败</strong>
                ${sessionScope['SPRING_SECURITY_LAST_EXCEPTION'].message }
        </div>
    </c:if>
    <c:remove var="SPRING_SECURITY_LAST_EXCEPTION" scope="session"/>
    <form class="form-signin" action="${ctx }/security/sms/do-login" method="post">
        <h2 class="form-signin-heading">请登录</h2>
        <div class="form-group">
            <label class="sr-only" for="inputPhone">手机号码</label>
            <input type="text"
                   id="inputPhone"
                   class="form-control"
                   placeholder="手机号码"
                   required="required"
                   name="phone"/>
        </div>
        <div class="form-group">
            <label class="sr-only" for="inputPassword">密码</label>
            <div class="input-group">
                <input type="text"
                       id="inputPassword"
                       class="form-control"
                       placeholder="短信验证码"
                       required="required"
                       name="userCode"/>
                <div class="input-group-addon addon-button" id="sendCode">
                    发送短信验证码
                </div>
            </div>
        </div>

        <label>记住登录
            <input type="checkbox" id="rememberme" name="remember-me"/>
        </label>
        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>
        <button class="btn btn-lg btn-primary btn-block" type="submit">登录</button>
    </form>
</div>
<!-- Modal -->
<div class="modal fade" id="prompt" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">提示</h4>
            </div>
            <div class="modal-body">
                <div class="text-center" id="message"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">


    function sendSMS() {
        // 显示灰色字体
        $("#sendCode").css({color: "#888"});
        $("#sendCode").text("120秒后重试");
        $("#sendCode").unbind("click");
        timerId = setInterval(function () {
            // 定时修改按钮上面的文字，提示120秒以后重试
            $("#sendCode").text(counter + "秒后重试");
            counter--;
            if (counter == 0) {
                cancelPause()
            }
        }, 1000);

        var phone = $("#inputPhone").val();
        if (!phone) {
            $("#prompt #message").text("请输入手机号码");
            $('#prompt').modal();
            return;
        }
        var url = "${pageContext.request.contextPath}/sms/verify";
        $.ajax(url, {
            method: "GET",
            dataType: "json",
            data: "phone=" + phone,
            success: function (xhr, text, response) {
                //console.log(arguments);
                //$("#prompt #message").html("短信发送成功!<br/>短信验证码在15分钟内有效。<br/>在手机上收到验证码后输入对应文本框。");
                //$('#prompt').modal();
                $("#succeed").removeClass("hide");
                $("#succeed").html("短信发送成功!<br/>短信验证码在15分钟内有效。<br/>在手机上收到验证码后输入对应文本框。");
            },
            error: function (xhr, error, response) {
                //console.log(arguments);
                $("#prompt #message").text("短信发送失败，请联系疯狂软件官方客服");
                $('#prompt').modal();
            }
        });
    }

    function cancelPause() {
        clearInterval(timerId);
        $("#sendCode").css({color: "#555"});
        $("#sendCode").text("发送短信验证码");
        $("#sendCode").bind("click", sendSMS);
        counter = 120;
    }


    var timerId;
    var counter = 120;
    $("#sendCode").bind("click", sendSMS);
</script>
</body>
</html>