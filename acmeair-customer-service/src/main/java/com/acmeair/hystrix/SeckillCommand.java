package com.acmeair.hystrix;

import com.acmeair.service.SeckillService;
import com.acmeair.web.dto.CouponInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class SeckillCommand implements SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillCommand.class);
    private final RestTemplate restTemplate;

    @Value("${customer.service.name:seckillServiceApp}")
    String seckillServiceName;

    SeckillCommand(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
            }
        });
    }

    @HystrixCommand
    public List<CouponInfo> getCoupons(String username) {
        String address = getSeckillServiceAddress();
        log.info("Sending GET request to remote coupon at {} with username {}", address, username);
        List<CouponInfo> resp = restTemplate.getForObject(address + "/query/coupons/{customerId}", List.class, username);
        ObjectMapper json = new ObjectMapper();
        List<CouponInfo> results = new ArrayList<>();
        for (Object coupon : resp) {
            try {
                results.add(json.readValue(json.writeValueAsString(coupon), CouponInfo.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    protected abstract String getSeckillServiceAddress();
}