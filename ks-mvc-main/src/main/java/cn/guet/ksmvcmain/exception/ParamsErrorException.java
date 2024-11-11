package cn.guet.ksmvcmain.exception;

public class ParamsErrorException extends RuntimeException{

    public ParamsErrorException() {
    }

    public ParamsErrorException(String message) {
        super(message);
    }
}
