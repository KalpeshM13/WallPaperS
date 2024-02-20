package com.kalpesh.wallpapers.Listeners;

import com.kalpesh.wallpapers.Models.CuratedApiResponse;

public interface CuratedResponseListener {
    void onFetch(CuratedApiResponse response, String message);
    void onError(String message);
}
