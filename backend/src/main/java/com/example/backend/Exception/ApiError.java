package com.example.backend.Exception;

import java.time.Instant;

/**
 * 統一的錯誤回應格式。
 * 目的：讓所有 API 出錯時，前端拿到的 JSON 結構都一樣，
 * 不用像現在這樣每支 API 猜欄位到底叫 error 還是 message。
 */
public class ApiError {

    private final String timestamp = Instant.now().toString();
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ApiError(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public String getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
}
