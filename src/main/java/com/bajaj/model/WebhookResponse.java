package com.bajaj.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {
    private String webhook;
    private String accessToken;
}
