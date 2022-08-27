org.springframework.cloud.openfeign.support.SpringEncoder do not support

x-application/hessian2 as a binary type and will give default charset UTF-8.

And when use feign with ApacheHttpClient, will encode body as string and cause data lose.

This package is created to make x-application/hessian2 binary type. May need updated
with different version.
