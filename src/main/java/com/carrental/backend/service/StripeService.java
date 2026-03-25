package com.carrental.backend.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentLink;
import com.stripe.param.PaymentLinkCreateParams;
import com.stripe.param.PaymentLinkCreateParams.LineItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.price.id}")
    private String stripePriceId;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createPaymentLink(long amountCents, String description) {
        try {
            // Create a line item using the existing price (custom amount)
            LineItem lineItem = LineItem.builder()
                    .setPrice(stripePriceId)
                    .setQuantity(1L)
                    .build();

            PaymentLinkCreateParams params = PaymentLinkCreateParams.builder()
                    .addLineItem(lineItem)
                    .setAfterCompletion(PaymentLinkCreateParams.AfterCompletion.builder()
                            .setType(PaymentLinkCreateParams.AfterCompletion.Type.REDIRECT)
                            .setRedirect(PaymentLinkCreateParams.AfterCompletion.Redirect.builder()
                                    .setUrl("https://www.nctrental.com/success") // after payment
                                    .build())
                            .build())
                    .build();

            PaymentLink paymentLink = PaymentLink.create(params);
            return paymentLink.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Stripe payment link: " + e.getMessage());
        }
    }
}
