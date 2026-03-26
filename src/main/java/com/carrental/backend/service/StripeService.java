package com.carrental.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentLink;
import com.stripe.param.PaymentLinkCreateParams;
import com.stripe.param.PaymentLinkCreateParams.LineItem;
import com.stripe.param.PaymentLinkCreateParams.LineItem.PriceData;
import com.stripe.param.PaymentLinkCreateParams.LineItem.PriceData.ProductData;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        logger.info("StripeService initialized");
    }

    public String createPaymentLink(long amountCents, String description) {
        try {
            logger.info("Creating payment link for amount {} cents", amountCents);
            // Create a line item with dynamic price data
            LineItem lineItem = LineItem.builder()
                    .setPriceData(
                            PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(amountCents)
                                    .setProductData(
                                            ProductData.builder()
                                                    .setName(description != null ? description : "Franchise Fee")
                                                    .build()
                                    )
                                    .build()
                    )
                    .setQuantity(1L)
                    .build();

            PaymentLinkCreateParams params = PaymentLinkCreateParams.builder()
                    .addLineItem(lineItem)
                    .setAfterCompletion(PaymentLinkCreateParams.AfterCompletion.builder()
                            .setType(PaymentLinkCreateParams.AfterCompletion.Type.REDIRECT)
                            .setRedirect(PaymentLinkCreateParams.AfterCompletion.Redirect.builder()
                                    .setUrl("https://www.nctrental.com/success")
                                    .build())
                            .build())
                    .build();

            PaymentLink paymentLink = PaymentLink.create(params);
            logger.info("Payment link created: {}", paymentLink.getUrl());
            return paymentLink.getUrl();
        } catch (StripeException e) {
            logger.error("Stripe API error: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating payment link", e);
            throw new RuntimeException("Failed to create payment link: " + e.getMessage());
        }
    }
}
