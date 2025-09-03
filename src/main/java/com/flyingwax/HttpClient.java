package com.flyingwax;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class HttpClient
{
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public HttpClient()
    {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<GroupData> getGroupData(String serverUrl, String groupId)
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                String url = serverUrl + "/v1/groups/" + groupId;
                log.info("Making HTTP request to: {}", url);
                Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute())
                {
                    log.info("HTTP response: {} {}", response.code(), response.message());
                    if (response.isSuccessful() && response.body() != null)
                    {
                        String json = response.body().string();
                        log.info("Received JSON response: {}", json.substring(0, Math.min(100, json.length())) + "...");
                        return objectMapper.readValue(json, GroupData.class);
                    }
                    else
                    {
                        log.warn("Failed to get group data: {} {}", response.code(), response.message());
                        return null;
                    }
                }
            }
            catch (IOException e)
            {
                log.error("Error getting group data", e);
                return null;
            }
        });
    }

    public CompletableFuture<Boolean> sendEvent(String serverUrl, String groupId, GameEvent event)
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                String url = serverUrl + "/v1/groups/" + groupId + "/events";
                String json = objectMapper.writeValueAsString(event);
                
                RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
                Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

                try (Response response = client.newCall(request).execute())
                {
                    return response.isSuccessful();
                }
            }
            catch (IOException e)
            {
                log.error("Error sending event", e);
                return false;
            }
        });
    }

    public CompletableFuture<BingoData> getBingoData(String serverUrl, String groupId)
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                String url = serverUrl + "/v1/bingo/boards/" + groupId;
                Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute())
                {
                    if (response.isSuccessful() && response.body() != null)
                    {
                        String json = response.body().string();
                        return objectMapper.readValue(json, BingoData.class);
                    }
                    else
                    {
                        log.warn("Failed to get bingo data: {}", response.code());
                        return null;
                    }
                }
            }
            catch (IOException e)
            {
                log.error("Error getting bingo data", e);
                return null;
            }
        });
    }

	public String downloadConfig(String url)
	{
		try
		{
			Request request = new Request.Builder()
				.url(url)
				.get()
				.build();

			try (Response response = client.newCall(request).execute())
			{
				if (response.isSuccessful() && response.body() != null)
				{
					return response.body().string();
				}
				else
				{
					log.warn("Failed to download config: HTTP {}", response.code());
				}
			}
		}
		catch (Exception e)
		{
			log.error("Error downloading config", e);
		}
		return null;
	}
} 