package com.example.d2clothings.interfaces;

public interface FirestoreOperationCallback<T> {
    void onSuccess(T result);
    void onFailure(String error);
}