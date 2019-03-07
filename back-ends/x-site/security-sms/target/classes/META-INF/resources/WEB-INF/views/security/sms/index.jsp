<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>首页</title>
</head>
<body>
	<div class="col-sm-12 col-md-6 col-md-offset-3 text-center">
		<form method="post" action="<c:url value="/security/sms/do-logout"/>">
			<input type="hidden"
				   name="${_csrf.parameterName}"
				   value="${_csrf.token}"/>
			<button>退出登录</button>
		</form>
	</div>
</body>
</html>