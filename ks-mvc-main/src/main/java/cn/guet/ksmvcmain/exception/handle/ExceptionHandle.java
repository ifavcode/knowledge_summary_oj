package cn.guet.ksmvcmain.exception.handle;

import cn.guet.ksmvcmain.exception.ParamsErrorException;
import com.example.kscommon.entity.AjaxResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(value = {ParamsErrorException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AjaxResult handleParamsException(ParamsErrorException e) {
        return AjaxResult.error(e.getMessage());
    }

    @ExceptionHandler(value = {BadCredentialsException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AjaxResult handleUsernameNotFoundException(BadCredentialsException e) {
        return AjaxResult.error(401, e.getMessage());
    }

//    @ExceptionHandler(value = {Exception.class})
//    @ResponseBody
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public AjaxResult exception(Exception e) {
//        return AjaxResult.error(e.getMessage());
//    }
}
