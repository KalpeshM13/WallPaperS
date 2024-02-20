package com.kalpesh.wallpapers.Listeners;

import com.kalpesh.wallpapers.Models.SearchApiResponse;

public interface SearchResponseListener {
    void onFetch(SearchApiResponse response, String message);
    void onError(String message);
}
