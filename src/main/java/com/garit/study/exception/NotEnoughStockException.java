package com.garit.study.exception;

public class NotEnoughStockException extends RuntimeException{
    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    /**
     * 예외가 발생한 근원적 Exception을 cause에 넣어서, Exception trace를 쭉 검색할 수 있다.
     */
    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }

}
